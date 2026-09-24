package com.airthings.lib.logging.facility

import com.airthings.lib.logging.LogLevel
import com.airthings.lib.logging.LogMessage
import com.airthings.lib.logging.platform.PlatformFileInputOutputImpl
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.TimeSource
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

/**
 * What a log line costs on the thread that writes it.
 *
 * Both measurements time the writes themselves and nothing else: the first calls the platform
 * append directly, the second goes through the facility and waits on the jobs it launched rather
 * than watching the file, so neither number carries polling latency.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalCoroutinesApi::class)
class FileLoggerFacilityCostTest {

    private lateinit var folder: String

    @BeforeTest
    fun setUp() {
        folder = NSTemporaryDirectory() + "kmplog-cost-" + TimeSource.Monotonic.markNow().hashCode()
        NSFileManager.defaultManager.createDirectoryAtPath(folder, true, null, null)
    }

    @AfterTest
    fun tearDown() {
        NSFileManager.defaultManager.removeItemAtPath(folder, null)
    }

    @Test
    fun `one append costs what it costs`() = runBlocking {
        val io = PlatformFileInputOutputImpl()
        val path = "$folder/raw.log"
        io.ensure(path)

        repeat(WARMUP) { io.append(path, "warm-$it\n") }

        val elapsed = measure { index -> io.append(path, "$LINE-$index\n") }
        report("append", elapsed)

        assertEquals(WARMUP + LINES, lineCount("raw.log"), "Every append has to produce one line")
        assertUnder(elapsed)
    }

    @Test
    fun `the facility adds what the queue adds`() = runBlocking {
        val job = SupervisorJob()
        val facility = FileLoggerFacility(
            minimumLogLevel = LogLevel.INFO,
            baseFolder = folder,
            // Mirrors what the convenience constructors build, so the queue being measured is the
            // one that ships. The scope is held here only to wait on the writes it launches.
            coroutineScope = CoroutineScope(Dispatchers.Default.limitedParallelism(1) + job),
            notifier = null,
        )

        repeat(WARMUP) { facility.log("warmup", LogLevel.INFO, LogMessage("warm-$it")) }
        job.drain()

        val elapsed = measure { index ->
            facility.log("cost", LogLevel.INFO, LogMessage("$LINE-$index"))
            job.drain()
        }
        report("facility", elapsed)

        val written = lines().filter { it.isNotBlank() }
        assertEquals(WARMUP + LINES, written.size, "Every call has to produce one line")
        repeat(LINES) { index ->
            assertTrue(written.any { it.endsWith("$LINE-$index") }, "$LINE-$index is missing")
        }
        assertUnder(elapsed)
    }

    // region helpers

    private suspend fun measure(write: suspend (Int) -> Unit): Duration {
        val started = TimeSource.Monotonic.markNow()
        repeat(LINES) { write(it) }
        return started.elapsedNow()
    }

    private fun report(what: String, elapsed: Duration) {
        println("$what: $LINES writes in $elapsed (${elapsed.inWholeMicroseconds / LINES} us each)")
    }

    private fun assertUnder(elapsed: Duration) {
        val perWrite = elapsed.inWholeMicroseconds / LINES
        assertTrue(
            perWrite < CEILING_MICROS,
            "A write costs $perWrite us, over the $CEILING_MICROS us this test was written against"
        )
    }

    private suspend fun Job.drain() {
        while (true) {
            val pending = children.toList()
            if (pending.isEmpty()) return
            pending.forEach { it.join() }
        }
    }

    private fun lines(name: String? = null): List<String> {
        val manager = NSFileManager.defaultManager
        val file = name ?: manager.contentsOfDirectoryAtPath(folder, null)
            .orEmpty()
            .map { "$it" }
            .firstOrNull { it.endsWith(".log") }
            ?: return emptyList()

        val text = NSString.stringWithContentsOfFile("$folder/$file", NSUTF8StringEncoding, null)
            ?: return emptyList()

        return "$text".lines()
    }

    private fun lineCount(name: String): Int = lines(name).count { it.isNotBlank() }

    // endregion

    private companion object {
        const val WARMUP = 20
        const val LINES = 500
        const val LINE = "line"

        /**
         * Generous on purpose: this guards against an order-of-magnitude regression, not against
         * the noise of a shared runner.
         */
        const val CEILING_MICROS = 2_000L
    }
}
