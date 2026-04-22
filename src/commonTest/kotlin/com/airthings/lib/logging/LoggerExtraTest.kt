package com.airthings.lib.logging

import com.airthings.lib.logging.facility.MockPrinterLoggerFacility
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Additional Logger coverage beyond `LoggerTest`:
 *   - the generic `log(level, ...)` overloads
 *   - the `log(lifecycle)` overloads
 *   - no-facility edge case
 */
class LoggerExtraTest {

    private val facility = ExtraCapturingFacility()
    private val logger = Logger(
        source = DEFAULT_SOURCE,
        decoration = null,
        coroutineScope = CoroutineScope(Dispatchers.Unconfined),
    )

    @BeforeTest
    fun setUp() {
        LoggerFacility.clear()
        LoggerFacility.register(FACILITY_NAME, facility)
    }

    @AfterTest
    fun tearDown() {
        LoggerFacility.clear()
    }

    // region generic log overloads

    @Test
    fun `log with level and message routes to the given level`() {
        logger.log(level = LogLevel.WARNING, message = LogMessage("hello"))

        val entry = facility.single()
        assertEquals(DEFAULT_SOURCE, entry.source)
        assertEquals(LogLevel.WARNING, entry.level)
        assertEquals("hello", entry.message?.message)
        assertNull(entry.error)
    }

    @Test
    fun `log with level and message and error routes with the error`() {
        val boom = RuntimeException("boom")

        logger.log(level = LogLevel.ERROR, message = LogMessage("hello"), error = boom)

        val entry = facility.single()
        assertEquals(LogLevel.ERROR, entry.level)
        assertSame(boom, entry.error)
    }

    @Test
    fun `log with source and level and message uses the custom source`() {
        logger.log(source = "custom", level = LogLevel.DEBUG, message = LogMessage("hello"))

        val entry = facility.single()
        assertEquals("custom", entry.source)
        assertEquals(LogLevel.DEBUG, entry.level)
    }

    @Test
    fun `log with source and level and message and error uses the custom source`() {
        val boom = RuntimeException("boom")

        logger.log(
            source = "custom",
            level = LogLevel.CRASH,
            message = LogMessage("hello"),
            error = boom,
        )

        val entry = facility.single()
        assertEquals("custom", entry.source)
        assertEquals(LogLevel.CRASH, entry.level)
        assertSame(boom, entry.error)
    }

    // endregion

    // region log(lifecycle) overloads

    @Test
    fun `log lifecycle emits at INFO level with the formatted event`() {
        val logger = Logger(
            source = DEFAULT_SOURCE,
            decoration = LogDecoration(prefix = "--> ", suffix = " <--", uppercase = false),
            coroutineScope = CoroutineScope(Dispatchers.Unconfined),
        )

        logger.log(LogLifecycle.RESUMED)

        val entry = facility.single()
        assertEquals(LogLevel.INFO, entry.level)
        assertEquals("--> resumed <--", entry.message?.message)
    }

    @Test
    fun `log lifecycle without decoration uppercases the event`() {
        logger.log(LogLifecycle.PAUSED)

        val entry = facility.single()
        assertEquals(LogLevel.INFO, entry.level)
        assertEquals("PAUSED", entry.message?.message)
    }

    @Test
    fun `log lifecycle with custom source routes through the source-based overload`() {
        logger.log(source = "custom", lifecycle = LogLifecycle.CREATED)

        val entry = facility.single()
        assertEquals("custom", entry.source)
        assertEquals(LogLevel.INFO, entry.level)
        assertEquals("CREATED", entry.message?.message)
    }

    // endregion

    // region no-facility edge case

    @Test
    fun `logging with no registered facilities does not throw`() {
        LoggerFacility.clear()

        logger.info("hello")

        // No capturing facility registered — nothing to assert. The point is the call doesn't throw.
        assertTrue(true)
    }

    @Test
    fun `Logger with source-only secondary constructor gets default decoration and scope`() {
        val minimal = Logger(source = "minimal")
        assertEquals("minimal", minimal.source)
        assertNull(minimal.decoration)
        assertNotNull(minimal.coroutineScope)
    }

    @Test
    fun `Logger with source + decoration secondary constructor uses default scope`() {
        val decoration = LogDecoration(prefix = "[", suffix = "]")
        val minimal = Logger(source = "minimal", decoration = decoration)

        assertEquals("minimal", minimal.source)
        assertSame(decoration, minimal.decoration)
        assertNotNull(minimal.coroutineScope)
    }

    // endregion

    private companion object {
        const val FACILITY_NAME = "capturing"
        const val DEFAULT_SOURCE = "test-source"
    }
}

/**
 * Minimal capturing facility — not extending [MockPrinterLoggerFacility] because we want to assert
 * on what gets logged. Named distinctly from [com.airthings.lib.logging.LoggerTest]'s capturing
 * facility to avoid a naming clash across the commonTest source set.
 */
private class ExtraCapturingFacility : LoggerFacility {
    data class Entry(val source: String, val level: LogLevel, val message: LogMessage?, val error: Throwable?)

    private val entries = mutableListOf<Entry>()

    override fun isEnabled(): Boolean = true

    override fun log(
        source: String,
        level: LogLevel,
        message: LogMessage,
    ) {
        entries += Entry(source, level, message, error = null)
    }

    override fun log(
        source: String,
        level: LogLevel,
        error: Throwable,
    ) {
        entries += Entry(source, level, message = null, error = error)
    }

    override fun log(
        source: String,
        level: LogLevel,
        message: LogMessage,
        error: Throwable,
    ) {
        entries += Entry(source, level, message, error)
    }

    fun single(): Entry {
        check(entries.size == 1) { "Expected exactly one captured entry, got ${entries.size}: $entries" }
        return entries.single()
    }
}
