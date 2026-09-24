package com.airthings.lib.logging.facility

import com.airthings.lib.logging.LogLevel
import com.airthings.lib.logging.LogMessage
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Two facilities built by the convenience constructors write one log file between them.
 *
 * What this covers is that the constructors work at all without a main dispatcher, and that the
 * dispatcher they share keeps one facility's line off the end of the other's. It does not stand in
 * for the arrangement on iOS, where the two facilities come from separate copies of this library
 * and so hold separate dispatchers - nor can the JVM reproduce the hazard there, because it appends
 * in append mode while Apple seeks and writes.
 */
class FileLoggerFacilitySharedFileTest {

    private lateinit var tempDir: File

    @BeforeTest
    fun setUp() {
        tempDir = Files.createTempDirectory("kmplog-shared-file-").toFile()
    }

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `the convenience constructor writes without a main dispatcher`() = runBlocking {
        val facility = FileLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
        )

        facility.log(source = "test", level = LogLevel.INFO, message = LogMessage("only line"))

        val file = awaitLines(expected = 1)
        assertTrue(
            file.readText().contains("only line"),
            "A facility built without an explicit scope has to be able to write"
        )
    }

    @Test
    fun `neither of two facilities loses a line to the other`() = runBlocking {
        val first = FileLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
        )
        val second = FileLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
        )

        repeat(LINES_EACH) { index ->
            first.log(source = "first", level = LogLevel.INFO, message = LogMessage("first-$index"))
            second.log(source = "second", level = LogLevel.INFO, message = LogMessage("second-$index"))
        }

        val lines = awaitLines(expected = LINES_EACH * 2).readLines().filter { it.isNotBlank() }
        repeat(LINES_EACH) { index ->
            assertTrue(lines.any { it.endsWith("first-$index") }, "first-$index was overwritten")
            assertTrue(lines.any { it.endsWith("second-$index") }, "second-$index was overwritten")
        }
    }

    @Test
    fun `a line is never half written`() = runBlocking {
        val first = FileLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
        )
        val second = FileLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = tempDir.absolutePath,
        )

        repeat(LINES_EACH) { index ->
            first.log(source = "first", level = LogLevel.INFO, message = LogMessage("aaaa-$index"))
            second.log(source = "second", level = LogLevel.INFO, message = LogMessage("bbbb-$index"))
        }

        val lines = awaitLines(expected = LINES_EACH * 2).readLines().filter { it.isNotBlank() }

        assertEquals(LINES_EACH * 2, lines.size, "Every call writes exactly one line")

        val whole = Regex("""(aaaa|bbbb)-\d+$""")
        lines.forEach { line ->
            assertTrue(whole.containsMatchIn(line), "A record was cut short or merged: $line")
            assertTrue(line.contains("aaaa-") != line.contains("bbbb-"), "Two writes landed in one line: $line")
        }
    }

    // region helpers

    private suspend fun awaitLines(expected: Int): File {
        val file = withTimeoutOrNull(DRAIN_TIMEOUT_MS) {
            while (true) {
                val candidate = logFiles().singleOrNull()
                if (candidate != null && candidate.readLines().count { it.isNotBlank() } >= expected) {
                    return@withTimeoutOrNull candidate
                }
                delay(POLL_MS)
            }
            @Suppress("UNREACHABLE_CODE")
            null
        }

        return requireNotNull(file) {
            val seen = logFiles().singleOrNull()?.readLines()?.count { it.isNotBlank() } ?: 0
            "Expected $expected lines within ${DRAIN_TIMEOUT_MS}ms, saw $seen"
        }
    }

    private fun logFiles(): List<File> =
        tempDir.listFiles { f -> f.isFile && f.name.endsWith(".log") }?.toList().orEmpty()

    // endregion

    private companion object {
        const val LINES_EACH = 200
        const val DRAIN_TIMEOUT_MS = 10_000L
        const val POLL_MS = 20L
    }
}
