package com.airthings.lib.logging.facility

import com.airthings.lib.logging.LogLevel
import com.airthings.lib.logging.LogMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The real [PrinterLoggerFacility] constructs a [com.airthings.lib.logging.platform.PlatformPrinterLoggerFacilityImpl]
 * internally and writes to stdout/NSLog/Log.d. We can't assert on that output, but we can assert
 * that calls complete without throwing, the class is enabled, its `toString` includes the inner
 * facility, and the companion `format` methods produce the documented strings.
 */
class PrinterLoggerFacilityTest {

    private val facility = PrinterLoggerFacility()

    @Test
    fun `isEnabled returns true`() {
        assertTrue(facility.isEnabled())
    }

    @Test
    fun `toString includes the inner platform facility`() {
        assertTrue(facility.toString().startsWith("PrinterLoggerFacility("))
        assertTrue(facility.toString().endsWith(")"))
    }

    // Intentionally no `log(...)` invocation tests — the Android runtime used for unit tests does
    // not mock `android.util.Log`, and mocking it would require a Robolectric dependency. The
    // platform-specific printer is exercised by integration tests on device, not here.

    @Test
    fun `format with message prefixes emoji and level label`() {
        val formatted = PrinterLoggerFacility.format(
            level = LogLevel.INFO,
            message = LogMessage("hello"),
        )

        assertEquals("${LogLevel.INFO.emoticon} INFO: hello", formatted)
    }

    @Test
    fun `format with message uses the level's emoticon for each level`() {
        LogLevel.entries.forEach { level ->
            val formatted = PrinterLoggerFacility.format(level = level, message = LogMessage("hello"))
            assertTrue(
                formatted.startsWith("${level.emoticon} $level: "),
                "Unexpected prefix for level $level: $formatted",
            )
        }
    }

    @Test
    fun `format with error includes the stack trace`() {
        val error = RuntimeException("boom")

        val formatted = PrinterLoggerFacility.format(level = LogLevel.ERROR, error = error)

        assertTrue(formatted.startsWith("${LogLevel.ERROR.emoticon} ERROR: "))
        assertTrue(
            formatted.contains("boom"),
            "Expected stack trace to contain 'boom', got: $formatted",
        )
    }
}
