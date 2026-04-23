package com.airthings.lib.logging

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Verifies that every Logger level method (debug/info/warning/error/crash) with every overload shape
 * routes to the right [LogLevel] with the expected source, message, arguments, and error.
 *
 * Structure: one helper per overload shape, plus five `@Test` methods per shape (one per level).
 * Total: 55 tests = 11 shapes × 5 levels.
 */
class LoggerTest {

    private val facility = CapturingFacility()
    private val logger = Logger(
        source = DEFAULT_SOURCE,
        decoration = null,
        // Unconfined makes launch { } run synchronously on the caller's thread so assertions
        // can run right after the log call without racing a dispatcher.
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

    // region shape 1: (message: String)

    @Test fun `debug with raw string`() = assertRawString(LogLevel.DEBUG) { debug(it) }

    @Test fun `info with raw string`() = assertRawString(LogLevel.INFO) { info(it) }

    @Test fun `warning with raw string`() = assertRawString(LogLevel.WARNING) { warning(it) }

    @Test fun `error with raw string`() = assertRawString(LogLevel.ERROR) { error(it) }

    @Test fun `crash with raw string`() = assertRawString(LogLevel.CRASH) { crash(it) }

    private fun assertRawString(
        level: LogLevel,
        call: Logger.(String) -> Unit,
    ) {
        logger.call("hello")

        val entry = facility.single()
        assertEquals(DEFAULT_SOURCE, entry.source)
        assertEquals(level, entry.level)
        assertEquals("hello", entry.message?.message)
        assertNull(entry.error)
    }

    // endregion

    // region shape 2: (message: String, arguments: Map)

    @Test fun `debug with string and argument map`() = assertStringAndMap(LogLevel.DEBUG) { msg, args ->
        debug(msg, args)
    }

    @Test fun `info with string and argument map`() = assertStringAndMap(LogLevel.INFO) { msg, args -> info(msg, args) }

    @Test fun `warning with string and argument map`() = assertStringAndMap(LogLevel.WARNING) { msg, args ->
        warning(msg, args)
    }

    @Test fun `error with string and argument map`() = assertStringAndMap(LogLevel.ERROR) { msg, args ->
        error(msg, args)
    }

    @Test fun `crash with string and argument map`() = assertStringAndMap(LogLevel.CRASH) { msg, args ->
        crash(msg, args)
    }

    private fun assertStringAndMap(
        level: LogLevel,
        call: Logger.(String, Map<String, Any?>) -> Unit,
    ) {
        val args = mapOf("key" to "value", "n" to 42)

        logger.call("hello", args)

        val entry = facility.single()
        assertEquals(level, entry.level)
        assertEquals("hello", entry.message?.message)
        assertEquals(listOf("key" to "value", "n" to 42), entry.message.pairs())
        assertNull(entry.error)
    }

    // endregion

    // region shape 3: (message: String, vararg arguments)

    @Test fun `debug with string and vararg`() = assertStringAndVararg(LogLevel.DEBUG) { msg, a, b -> debug(msg, a, b) }

    @Test fun `info with string and vararg`() = assertStringAndVararg(LogLevel.INFO) { msg, a, b -> info(msg, a, b) }

    @Test fun `warning with string and vararg`() = assertStringAndVararg(LogLevel.WARNING) { msg, a, b ->
        warning(msg, a, b)
    }

    @Test fun `error with string and vararg`() = assertStringAndVararg(LogLevel.ERROR) { msg, a, b -> error(msg, a, b) }

    @Test fun `crash with string and vararg`() = assertStringAndVararg(LogLevel.CRASH) { msg, a, b -> crash(msg, a, b) }

    private fun assertStringAndVararg(
        level: LogLevel,
        call: Logger.(String, Pair<String, Any?>, Pair<String, Any?>) -> Unit,
    ) {
        logger.call("hello", "key" to "value", "n" to 42)

        val entry = facility.single()
        assertEquals(level, entry.level)
        assertEquals("hello", entry.message?.message)
        assertEquals(listOf("key" to "value", "n" to 42), entry.message.pairs())
        assertNull(entry.error)
    }

    // endregion

    // region shape 4: (message: LogMessage)

    @Test fun `debug with LogMessage`() = assertLogMessage(LogLevel.DEBUG) { debug(it) }

    @Test fun `info with LogMessage`() = assertLogMessage(LogLevel.INFO) { info(it) }

    @Test fun `warning with LogMessage`() = assertLogMessage(LogLevel.WARNING) { warning(it) }

    @Test fun `error with LogMessage`() = assertLogMessage(LogLevel.ERROR) { error(it) }

    @Test fun `crash with LogMessage`() = assertLogMessage(LogLevel.CRASH) { crash(it) }

    private fun assertLogMessage(
        level: LogLevel,
        call: Logger.(LogMessage) -> Unit,
    ) {
        val message = LogMessage("hello", mapOf("k" to "v"))

        logger.call(message)

        val entry = facility.single()
        assertEquals(level, entry.level)
        assertSame(message, entry.message)
        assertNull(entry.error)
    }

    // endregion

    // region shape 5: (message: String, error: Throwable)

    @Test fun `debug with string and error`() = assertStringAndError(LogLevel.DEBUG) { msg, err -> debug(msg, err) }

    @Test fun `info with string and error`() = assertStringAndError(LogLevel.INFO) { msg, err -> info(msg, err) }

    @Test fun `warning with string and error`() = assertStringAndError(LogLevel.WARNING) { msg, err ->
        warning(msg, err)
    }

    @Test fun `error with string and error`() = assertStringAndError(LogLevel.ERROR) { msg, err -> error(msg, err) }

    @Test fun `crash with string and error`() = assertStringAndError(LogLevel.CRASH) { msg, err -> crash(msg, err) }

    private fun assertStringAndError(
        level: LogLevel,
        call: Logger.(String, Throwable) -> Unit,
    ) {
        val boom = RuntimeException("boom")

        logger.call("hello", boom)

        val entry = facility.single()
        assertEquals(level, entry.level)
        assertEquals("hello", entry.message?.message)
        assertSame(boom, entry.error)
    }

    // endregion

    // region shape 6: (message: String, arguments: Map, error: Throwable)

    @Test fun `debug with string and map and error`() = assertStringMapError(LogLevel.DEBUG) { msg, args, err ->
        debug(msg, args, err)
    }

    @Test fun `info with string and map and error`() = assertStringMapError(LogLevel.INFO) { msg, args, err ->
        info(msg, args, err)
    }

    @Test fun `warning with string and map and error`() = assertStringMapError(LogLevel.WARNING) { msg, args, err ->
        warning(msg, args, err)
    }

    @Test fun `error with string and map and error`() = assertStringMapError(LogLevel.ERROR) { msg, args, err ->
        error(msg, args, err)
    }

    @Test fun `crash with string and map and error`() = assertStringMapError(LogLevel.CRASH) { msg, args, err ->
        crash(msg, args, err)
    }

    private fun assertStringMapError(
        level: LogLevel,
        call: Logger.(String, Map<String, Any?>, Throwable) -> Unit,
    ) {
        val args = mapOf("k" to "v")
        val boom = RuntimeException("boom")

        logger.call("hello", args, boom)

        val entry = facility.single()
        assertEquals(level, entry.level)
        assertEquals("hello", entry.message?.message)
        assertEquals(listOf("k" to "v"), entry.message.pairs())
        assertSame(boom, entry.error)
    }

    // endregion

    // region shape 7: (message: String, vararg arguments, error: Throwable)

    @Test fun `debug with string and vararg and error`() = assertStringVarargError(LogLevel.DEBUG) { msg, a, err ->
        debug(msg, a, error = err)
    }

    @Test fun `info with string and vararg and error`() = assertStringVarargError(LogLevel.INFO) { msg, a, err ->
        info(msg, a, error = err)
    }

    @Test fun `warning with string and vararg and error`() = assertStringVarargError(LogLevel.WARNING) { msg, a, err ->
        warning(msg, a, error = err)
    }

    @Test fun `error with string and vararg and error`() = assertStringVarargError(LogLevel.ERROR) { msg, a, err ->
        error(msg, a, error = err)
    }

    @Test fun `crash with string and vararg and error`() = assertStringVarargError(LogLevel.CRASH) { msg, a, err ->
        crash(msg, a, error = err)
    }

    private fun assertStringVarargError(
        level: LogLevel,
        call: Logger.(String, Pair<String, Any?>, Throwable) -> Unit,
    ) {
        val boom = RuntimeException("boom")

        logger.call("hello", "k" to "v", boom)

        val entry = facility.single()
        assertEquals(level, entry.level)
        assertEquals("hello", entry.message?.message)
        assertEquals(listOf("k" to "v"), entry.message.pairs())
        assertSame(boom, entry.error)
    }

    // endregion

    // region shape 8: (message: LogMessage, error: Throwable)

    @Test fun `debug with LogMessage and error`() = assertLogMessageError(LogLevel.DEBUG) { msg, err ->
        debug(msg, err)
    }

    @Test fun `info with LogMessage and error`() = assertLogMessageError(LogLevel.INFO) { msg, err -> info(msg, err) }

    @Test fun `warning with LogMessage and error`() = assertLogMessageError(LogLevel.WARNING) { msg, err ->
        warning(msg, err)
    }

    @Test fun `error with LogMessage and error`() = assertLogMessageError(LogLevel.ERROR) { msg, err ->
        error(msg, err)
    }

    @Test fun `crash with LogMessage and error`() = assertLogMessageError(LogLevel.CRASH) { msg, err ->
        crash(msg, err)
    }

    private fun assertLogMessageError(
        level: LogLevel,
        call: Logger.(LogMessage, Throwable) -> Unit,
    ) {
        val message = LogMessage("hello")
        val boom = RuntimeException("boom")

        logger.call(message, boom)

        val entry = facility.single()
        assertEquals(level, entry.level)
        assertSame(message, entry.message)
        assertSame(boom, entry.error)
    }

    // endregion

    // region shape 9: (source: String, message: String)

    @Test fun `debug with custom source and string`() = assertCustomSourceString(LogLevel.DEBUG) { src, msg ->
        debug(src, msg)
    }

    @Test fun `info with custom source and string`() = assertCustomSourceString(LogLevel.INFO) { src, msg ->
        info(src, msg)
    }

    @Test fun `warning with custom source and string`() = assertCustomSourceString(LogLevel.WARNING) { src, msg ->
        warning(src, msg)
    }

    @Test fun `error with custom source and string`() = assertCustomSourceString(LogLevel.ERROR) { src, msg ->
        error(src, msg)
    }

    @Test fun `crash with custom source and string`() = assertCustomSourceString(LogLevel.CRASH) { src, msg ->
        crash(src, msg)
    }

    private fun assertCustomSourceString(
        level: LogLevel,
        call: Logger.(String, String) -> Unit,
    ) {
        logger.call("custom", "hello")

        val entry = facility.single()
        assertEquals("custom", entry.source)
        assertEquals(level, entry.level)
        assertEquals("hello", entry.message?.message)
    }

    // endregion

    // region shape 10: (source: String, message: LogMessage)

    @Test fun `debug with custom source and LogMessage`() = assertCustomSourceLogMessage(LogLevel.DEBUG) { src, msg ->
        debug(src, msg)
    }

    @Test fun `info with custom source and LogMessage`() = assertCustomSourceLogMessage(LogLevel.INFO) { src, msg ->
        info(src, msg)
    }

    @Test fun `warning with custom source and LogMessage`() = assertCustomSourceLogMessage(LogLevel.WARNING) {
            src,
            msg,
        ->
        warning(src, msg)
    }

    @Test fun `error with custom source and LogMessage`() = assertCustomSourceLogMessage(LogLevel.ERROR) { src, msg ->
        error(src, msg)
    }

    @Test fun `crash with custom source and LogMessage`() = assertCustomSourceLogMessage(LogLevel.CRASH) { src, msg ->
        crash(src, msg)
    }

    private fun assertCustomSourceLogMessage(
        level: LogLevel,
        call: Logger.(String, LogMessage) -> Unit,
    ) {
        val message = LogMessage("hello")

        logger.call("custom", message)

        val entry = facility.single()
        assertEquals("custom", entry.source)
        assertEquals(level, entry.level)
        assertSame(message, entry.message)
    }

    // endregion

    // region shape 11: (source: String, message: LogMessage, error: Throwable)

    @Test fun `debug with custom source and LogMessage and error`() = assertCustomSourceLogMessageError(
        LogLevel.DEBUG,
    ) {
            src,
            msg,
            err,
        ->
        debug(src, msg, err)
    }

    @Test fun `info with custom source and LogMessage and error`() = assertCustomSourceLogMessageError(LogLevel.INFO) {
            src,
            msg,
            err,
        ->
        info(src, msg, err)
    }

    @Test fun `warning with custom source and LogMessage and error`() = assertCustomSourceLogMessageError(
        LogLevel.WARNING,
    ) {
            src,
            msg,
            err,
        ->
        warning(src, msg, err)
    }

    @Test fun `error with custom source and LogMessage and error`() = assertCustomSourceLogMessageError(
        LogLevel.ERROR,
    ) {
            src,
            msg,
            err,
        ->
        error(src, msg, err)
    }

    @Test fun `crash with custom source and LogMessage and error`() = assertCustomSourceLogMessageError(
        LogLevel.CRASH,
    ) {
            src,
            msg,
            err,
        ->
        crash(src, msg, err)
    }

    private fun assertCustomSourceLogMessageError(
        level: LogLevel,
        call: Logger.(String, LogMessage, Throwable) -> Unit,
    ) {
        val message = LogMessage("hello")
        val boom = RuntimeException("boom")

        logger.call("custom", message, boom)

        val entry = facility.single()
        assertEquals("custom", entry.source)
        assertEquals(level, entry.level)
        assertSame(message, entry.message)
        assertSame(boom, entry.error)
    }

    // endregion

    private companion object {
        const val FACILITY_NAME = "capturing"
        const val DEFAULT_SOURCE = "test-source"
    }
}

private fun LogMessage?.pairs(): List<Pair<String, Any?>>? = this?.args?.map { it.label to it.value }

/**
 * In-memory [LoggerFacility] that records every logged entry for later inspection.
 */
private class CapturingFacility : LoggerFacility {
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
