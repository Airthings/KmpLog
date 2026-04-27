package com.airthings.lib.logging.facility

import com.airthings.lib.logging.LogArg
import com.airthings.lib.logging.LogDate
import com.airthings.lib.logging.LogLevel
import com.airthings.lib.logging.LogMessage
import com.airthings.lib.logging.platform.PlatformFileInputOutputNotifier
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest

class JsonLoggerFacilityTest {

    private lateinit var tempDir: File
    private val scope = CoroutineScope(Dispatchers.Unconfined)

    @BeforeTest
    fun setUp() {
        tempDir = Files.createTempDirectory("kmplog-json-facility-").toFile()
    }

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `log writes a valid single-entry JSON array`() {
        val facility = newFacility()

        facility.log(source = "src", level = LogLevel.WARNING, message = LogMessage("caution"))

        val contents = soleJsonFile().readText()
        assertValidJsonArrayShape(contents, expectedEntries = 1)
        assertContains(contents, "\"source\":\"src\"")
        assertContains(contents, "\"level\":\"WARNING\"")
        assertContains(contents, "\"message\":\"caution\"")
    }

    @Test
    fun `log appends entries with a comma separator`() {
        val facility = newFacility()

        facility.log(source = "src", level = LogLevel.INFO, message = LogMessage("first"))
        facility.log(source = "src", level = LogLevel.INFO, message = LogMessage("second"))
        facility.log(source = "src", level = LogLevel.INFO, message = LogMessage("third"))

        val contents = soleJsonFile().readText()
        assertValidJsonArrayShape(contents, expectedEntries = 3)
        assertContains(contents, "\"message\":\"first\"")
        assertContains(contents, "\"message\":\"second\"")
        assertContains(contents, "\"message\":\"third\"")
    }

    @Test
    fun `log drops entries below the minimum level`() {
        val facility = newFacility(minimumLogLevel = LogLevel.WARNING)

        facility.log(source = "src", level = LogLevel.INFO, message = LogMessage("quiet"))

        assertEquals(0, jsonFiles().size)
    }

    @Test
    fun `log with error writes the stack trace under an error key`() {
        val facility = newFacility()
        val boom = IllegalStateException("oops")

        facility.log(source = "src", level = LogLevel.ERROR, error = boom)

        val contents = soleJsonFile().readText()
        assertValidJsonArrayShape(contents, expectedEntries = 1)
        assertContains(contents, "\"error\":\"")
        assertContains(contents, "IllegalStateException")
    }

    @Test
    fun `log with message and error writes a single entry with both fields`() {
        val facility = newFacility()
        val boom = IllegalStateException("oops")

        facility.log(
            source = "src",
            level = LogLevel.ERROR,
            message = LogMessage("hello"),
            error = boom,
        )

        val contents = soleJsonFile().readText()
        assertValidJsonArrayShape(contents, expectedEntries = 1)
        assertContains(contents, "\"message\":\"hello\"")
        assertContains(contents, "\"error\":\"")
    }

    @Test
    fun `log includes args under the args key`() {
        val facility = newFacility()
        val message = LogMessage(
            "request",
            args = listOf(LogArg("status", 200), LogArg("path", "/x")),
        )

        facility.log(source = "src", level = LogLevel.INFO, message = message)

        val contents = soleJsonFile().readText()
        assertValidJsonArrayShape(contents, expectedEntries = 1)
        assertContains(contents, "\"args\":{")
        assertContains(contents, "\"status\":\"200\"")
        assertContains(contents, "\"path\":\"\\/x\"")
    }

    @Test
    fun `log escapes JSON-special characters in the message`() {
        val facility = newFacility()

        facility.log(
            source = "src",
            level = LogLevel.INFO,
            message = LogMessage("line1\nline2\twith \"quotes\""),
        )

        val contents = soleJsonFile().readText()
        assertValidJsonArrayShape(contents, expectedEntries = 1)
        assertContains(contents, "line1\\nline2\\twith \\\"quotes\\\"")
        assertFalse(
            contents.contains("line1\nline2"),
            "Raw newline should not appear inside a JSON value: $contents",
        )
    }

    @Test
    fun `a second facility appends to the same file instead of overwriting it`() {
        // Simulate an app restart: one facility writes an entry, then a fresh facility
        // targeting the same folder should add a second entry rather than re-init the file.
        newFacility().log(source = "src", level = LogLevel.INFO, message = LogMessage("prior"))
        val fileAfterFirst = soleJsonFile()
        val sizeAfterFirst = fileAfterFirst.length()

        newFacility().log(source = "src", level = LogLevel.INFO, message = LogMessage("new"))

        val contents = soleJsonFile().readText()
        assertValidJsonArrayShape(contents, expectedEntries = 2)
        assertContains(contents, "\"message\":\"prior\"")
        assertContains(contents, "\"message\":\"new\"")
        assertTrue(soleJsonFile().length() > sizeAfterFirst)
    }

    @Test
    fun `isEnabled is true and toString identifies the facility`() {
        val facility = newFacility()
        assertTrue(facility.isEnabled())
        assertContains(facility.toString(), "JsonLoggerFacility(")
    }

    @Test
    fun `notifier is invoked when a JSON log file is first opened`() {
        val opened = mutableListOf<String>()
        val notifier = object : PlatformFileInputOutputNotifier {
            override fun onLogFolderInvalid(folder: String) = Unit
            override fun onLogFileOpened(path: String) {
                opened += path
            }
            override fun onLogFileClosed(path: String) = Unit
        }
        val facility = newFacility(notifier = notifier)

        facility.log(source = "src", level = LogLevel.INFO, message = LogMessage("first"))

        assertEquals(1, opened.size)
        assertTrue(opened.first().endsWith(".json"))
    }

    @Test
    fun `secondary constructor with baseFolder+scope+notifier defaults to WARNING`() {
        val facility = JsonLoggerFacility(
            baseFolder = tempDir.absolutePath,
            scope = scope,
            notifier = null,
        )
        facility.log(source = "src", level = LogLevel.INFO, message = LogMessage("filtered out"))
        assertEquals(0, jsonFiles().size)
    }

    @Test
    fun `secondary constructor with minimumLogLevel+baseFolder+notifier uses default scope`() {
        // The default scope uses Dispatchers.Main which isn't available in plain JUnit; verify
        // construction succeeds without invoking log() on this instance.
        JsonLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            notifier = null,
        )
    }

    @Test
    fun `files() returns the JSON log files in the folder`() = runTest {
        val facility = newFacility()
        File(tempDir, "2024-03-05.json").createNewFile()
        File(tempDir, "2024-03-06.json").createNewFile()

        val files = facility.files()

        assertEquals(2, files.size)
    }

    @Test
    fun `files(date) returns only JSON log files newer than the cutoff`() = runTest {
        val facility = newFacility()
        File(tempDir, "2024-03-05.json").createNewFile()
        File(tempDir, "2024-03-07.json").createNewFile()

        val files = facility.files(LogDate(2024, 3, 6))

        assertEquals(1, files.size)
        assertTrue(files.first().endsWith("2024-03-07.json"))
    }

    // region helpers

    private fun newFacility(
        minimumLogLevel: LogLevel = LogLevel.INFO,
        notifier: PlatformFileInputOutputNotifier? = null,
    ): JsonLoggerFacility = JsonLoggerFacility(
        minimumLogLevel = minimumLogLevel,
        baseFolder = tempDir.absolutePath,
        coroutineScope = scope,
        notifier = notifier,
    )

    private fun jsonFiles(): List<File> =
        tempDir.listFiles { f -> f.isFile && f.name.endsWith(".json") }?.toList().orEmpty()

    private fun soleJsonFile(): File {
        val files = jsonFiles()
        check(files.size == 1) { "Expected exactly one .json file, got ${files.map { it.name }}" }
        return files.single()
    }

    /**
     * Verifies the file contents look like a well-formed JSON array of `expectedEntries`
     * top-level objects, without pulling in a full JSON parser.
     *
     * Structural invariants checked:
     *  - Starts with `[` and ends with `]`.
     *  - Brace count matches (balanced `{` and `}`).
     *  - Exactly `expectedEntries - 1` top-level `,` separators at depth 0
     *    (after stripping the outer `[...]`).
     */
    private fun assertValidJsonArrayShape(
        contents: String,
        expectedEntries: Int,
    ) {
        assertTrue(contents.startsWith("["), "Must start with '[': $contents")
        assertTrue(contents.endsWith("]"), "Must end with ']': $contents")

        var depth = 0
        var topLevelSeparators = 0
        var objectCount = 0
        var inString = false
        var escaped = false
        contents.substring(1, contents.length - 1).forEach { ch ->
            if (escaped) {
                escaped = false
                return@forEach
            }
            when {
                ch == '\\' && inString -> escaped = true
                ch == '"' -> inString = !inString
                inString -> Unit
                ch == '{' -> {
                    if (depth == 0) objectCount++
                    depth++
                }
                ch == '}' -> depth--
                ch == ',' && depth == 0 -> topLevelSeparators++
            }
        }
        assertEquals(0, depth, "Unbalanced braces: $contents")
        assertEquals(
            expectedEntries,
            objectCount,
            "Expected $expectedEntries top-level objects: $contents",
        )
        assertEquals(
            (expectedEntries - 1).coerceAtLeast(0),
            topLevelSeparators,
            "Expected ${expectedEntries - 1} top-level commas: $contents",
        )
    }

    // endregion
}
