package com.airthings.lib.logging.facility

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
 * JVM-side coverage for [FileLoggerFacility]. Uses a real tmp directory so we exercise the
 * actual platform I/O path rather than a mock.
 */
class FileLoggerFacilityTest {

    private lateinit var tempDir: File
    private val scope = CoroutineScope(Dispatchers.Unconfined)

    @BeforeTest
    fun setUp() {
        tempDir = Files.createTempDirectory("kmplog-file-facility-").toFile()
    }

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `isEnabled is always true`() {
        val facility = FileLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )
        assertTrue(facility.isEnabled())
    }

    @Test
    fun `toString describes the facility`() {
        val facility = FileLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )
        assertTrue(facility.toString().startsWith("FileLoggerFacility("))
    }

    @Test
    fun `secondary constructor with baseFolder+scope+notifier defaults to WARNING`() {
        val facility = FileLoggerFacility(
            baseFolder = tempDir.absolutePath,
            scope = scope,
            notifier = null,
        )
        facility.log(source = "src", level = LogLevel.INFO, message = LogMessage("x"))
        // INFO < WARNING default → nothing written.
        assertEquals(0, logFiles().size)
    }

    @Test
    fun `secondary constructor with minimumLogLevel+baseFolder+scope accepts an explicit level`() {
        val facility = FileLoggerFacility(
            minimumLogLevel = LogLevel.DEBUG,
            baseFolder = tempDir.absolutePath,
            scope = scope,
        )
        facility.log(source = "src", level = LogLevel.INFO, message = LogMessage("hello"))

        val contents = soleLogFile().readText()
        assertContains(contents, "INFO: hello")
    }

    @Test
    fun `log writes a formatted line to today's file`() {
        val facility = FileLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )

        facility.log(source = "src", level = LogLevel.WARNING, message = LogMessage("caution"))

        val contents = soleLogFile().readText()
        assertContains(contents, "WARNING: caution")
        assertTrue(contents.endsWith("\n"), "Each entry should end with a newline: $contents")
    }

    @Test
    fun `log drops messages below the minimum level`() {
        val facility = FileLoggerFacility(
            minimumLogLevel = LogLevel.WARNING,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )

        facility.log(source = "src", level = LogLevel.INFO, message = LogMessage("quiet"))
        facility.log(source = "src", level = LogLevel.DEBUG, message = LogMessage("quieter"))

        assertEquals(0, logFiles().size, "INFO and DEBUG should be filtered out before the file is touched")
    }

    @Test
    fun `log with error writes the stack trace`() {
        val facility = FileLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )
        val boom = IllegalStateException("oops")

        facility.log(source = "src", level = LogLevel.ERROR, error = boom)

        val contents = soleLogFile().readText()
        assertContains(contents, "IllegalStateException")
        assertContains(contents, "oops")
    }

    @Test
    fun `log with message and error writes both in separate lines`() {
        val facility = FileLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )
        val boom = IllegalStateException("oops")

        facility.log(source = "src", level = LogLevel.ERROR, message = LogMessage("hello"), error = boom)

        val lines = soleLogFile().readLines()
        assertTrue(lines.any { it.contains("ERROR: hello") })
        assertTrue(lines.any { it.contains("IllegalStateException") })
    }

    @Test
    fun `files() returns all files in the folder`() = runTest {
        val facility = FileLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )
        File(tempDir, "2024-03-05.log").createNewFile()
        File(tempDir, "2024-03-06.log").createNewFile()

        val files = facility.files()

        assertEquals(2, files.size)
    }

    @Test
    fun `files(date) returns only files newer than the cutoff`() = runTest {
        val facility = FileLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )
        File(tempDir, "2024-03-05.log").createNewFile()
        File(tempDir, "2024-03-07.log").createNewFile()

        val files = facility.files(LogDate(2024, 3, 6))

        assertEquals(1, files.size)
        assertTrue(files.first().endsWith("2024-03-07.log"))
    }

    @Test
    fun `delete removes a file by name`() = runTest {
        val facility = FileLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )
        val file = File(tempDir, "gone.log").apply { createNewFile() }
        assertTrue(file.exists())

        facility.delete("gone.log")

        assertFalse(file.exists())
    }

    @Test
    fun `deleteAbsolute removes a file by absolute path`() = runTest {
        val facility = FileLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )
        val file = File(tempDir, "gone.log").apply { createNewFile() }

        facility.deleteAbsolute(file.absolutePath)

        assertFalse(file.exists())
    }

    @Test
    fun `listing exposes the underlying platform directory listing`() {
        val facility = FileLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = null,
        )
        assertTrue(facility.listing.toString().isNotBlank())
    }

    @Test
    fun `notifier is invoked when a log file is first opened`() {
        val opened = mutableListOf<String>()
        val notifier = object : PlatformFileInputOutputNotifier {
            override fun onLogFolderInvalid(folder: String) = Unit
            override fun onLogFileOpened(path: String) {
                opened += path
            }
            override fun onLogFileClosed(path: String) = Unit
        }
        val facility = FileLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
            coroutineScope = scope,
            notifier = notifier,
        )

        facility.log(source = "src", level = LogLevel.INFO, message = LogMessage("first"))

        assertEquals(1, opened.size)
        assertTrue(opened.first().endsWith(".log"))
    }

    // region helpers

    private fun logFiles(): List<File> =
        tempDir.listFiles { f -> f.isFile && f.name.endsWith(".log") }?.toList().orEmpty()

    private fun soleLogFile(): File {
        val files = logFiles()
        check(files.size == 1) { "Expected exactly one .log file, got ${files.map { it.name }}" }
        return files.single()
    }

    // endregion
}
