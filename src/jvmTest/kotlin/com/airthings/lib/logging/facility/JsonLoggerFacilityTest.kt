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

/**
 * JVM-side coverage for [JsonLoggerFacility]. Uses a real tmp directory for actual file I/O.
 */
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
    fun `isEnabled is always true`() {
        val facility = JsonLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )
        assertTrue(facility.isEnabled())
    }

    @Test
    fun `toString describes the facility`() {
        val facility = JsonLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )
        assertTrue(facility.toString().startsWith("JsonLoggerFacility("))
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
        // Default scope uses Dispatchers.Main which isn't available in plain JUnit — just verify
        // construction succeeds; the notifier check below covers behavior with an explicit scope.
        JsonLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            notifier = null,
        )
    }

    @Test
    fun `log writes a JSON array with the entry`() {
        val facility = JsonLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )

        facility.log(source = "src", level = LogLevel.WARNING, message = LogMessage("caution"))

        val contents = soleJsonFile().readText()
        assertTrue(contents.startsWith("["))
        assertTrue(contents.endsWith("]"))
        assertContains(contents, "\"source\":\"src\"")
        assertContains(contents, "\"level\":\"WARNING\"")
        assertContains(contents, "\"message\":\"caution\"")
    }

    @Test
    fun `log drops messages below the minimum level`() {
        val facility = JsonLoggerFacility(
            minimumLogLevel = LogLevel.WARNING,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )

        facility.log(source = "src", level = LogLevel.INFO, message = LogMessage("quiet"))

        assertEquals(0, jsonFiles().size)
    }

    @Test
    fun `log appends multiple entries to the same file`() {
        val facility = JsonLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )

        facility.log(source = "src", level = LogLevel.INFO, message = LogMessage("first"))
        facility.log(source = "src", level = LogLevel.INFO, message = LogMessage("second"))

        val contents = soleJsonFile().readText()
        assertContains(contents, "first")
        assertContains(contents, "second")
        // NOTE: current output is invalid JSON — see bug_json_logger_invalid_array in memory.
        // This assertion only verifies both entries landed in the file, not that the result parses.
    }

    @Test
    fun `log with error writes the stack trace field`() {
        val facility = JsonLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )
        val boom = IllegalStateException("oops")

        facility.log(source = "src", level = LogLevel.ERROR, error = boom)

        val contents = soleJsonFile().readText()
        assertContains(contents, "\"error\":")
        assertContains(contents, "IllegalStateException")
        assertContains(contents, "oops")
    }

    @Test
    fun `log with message+error includes both fields`() {
        val facility = JsonLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )
        val boom = IllegalStateException("oops")

        facility.log(
            source = "src",
            level = LogLevel.ERROR,
            message = LogMessage("hello"),
            error = boom,
        )

        val contents = soleJsonFile().readText()
        assertContains(contents, "\"message\":\"hello\"")
        assertContains(contents, "\"error\":")
    }

    @Test
    fun `log includes args when the LogMessage carries them`() {
        val facility = JsonLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )
        val message = LogMessage("request", args = listOf(LogArg("status", 200), LogArg("path", "/x")))

        facility.log(source = "src", level = LogLevel.INFO, message = message)

        val contents = soleJsonFile().readText()
        // NOTE: current output emits the args block as a bare object without an "args": wrapper,
        // which produces invalid JSON. See bug_json_logger_invalid_array in memory. This test
        // only checks the argument values made it into the file.
        assertContains(contents, "\"status\":\"200\"")
        assertContains(contents, "\"path\":\"\\/x\"")
    }

    @Test
    fun `files() returns the JSON log files in the folder`() = runTest {
        val facility = JsonLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )
        File(tempDir, "2024-03-05.json").createNewFile()
        File(tempDir, "2024-03-06.json").createNewFile()

        val files = facility.files()

        assertEquals(2, files.size)
    }

    @Test
    fun `files(date) returns only JSON log files newer than the cutoff`() = runTest {
        val facility = JsonLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )
        File(tempDir, "2024-03-05.json").createNewFile()
        File(tempDir, "2024-03-07.json").createNewFile()

        val files = facility.files(LogDate(2024, 3, 6))

        assertEquals(1, files.size)
        assertTrue(files.first().endsWith("2024-03-07.json"))
    }

    @Test
    fun `notifier fires when a json log file is first opened`() {
        val opened = mutableListOf<String>()
        val notifier = object : PlatformFileInputOutputNotifier {
            override fun onLogFolderInvalid(folder: String) = Unit
            override fun onLogFileOpened(path: String) {
                opened += path
            }
            override fun onLogFileClosed(path: String) = Unit
        }
        val facility = JsonLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = notifier,
        )

        facility.log(source = "src", level = LogLevel.INFO, message = LogMessage("first"))

        assertEquals(1, opened.size)
        assertTrue(opened.first().endsWith(".json"))
    }

    @Test
    fun `log escapes JSON-special characters in the message`() {
        val facility = JsonLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )

        facility.log(
            source = "src",
            level = LogLevel.INFO,
            message = LogMessage("line1\nline2\twith \"quotes\" and \\slashes"),
        )

        val contents = soleJsonFile().readText()
        assertContains(contents, "line1\\nline2\\twith \\\"quotes\\\" and \\\\slashes")
        // The raw newline character itself should NOT appear inside the JSON value — it must be
        // escaped.
        assertFalse(contents.contains("line1\nline2"))
    }

    // region helpers

    private fun jsonFiles(): List<File> =
        tempDir.listFiles { f -> f.isFile && f.name.endsWith(".json") }?.toList().orEmpty()

    private fun soleJsonFile(): File {
        val files = jsonFiles()
        check(files.size == 1) { "Expected exactly one .json file, got ${files.map { it.name }}" }
        return files.single()
    }

    // endregion
}
