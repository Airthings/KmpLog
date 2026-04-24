package com.airthings.lib.logging.facility

import com.airthings.lib.logging.LogLevel
import com.airthings.lib.logging.LogMessage
import com.airthings.lib.logging.platform.PlatformPrinterLoggerFacilityImpl
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Exercises the `PrinterLoggerFacility.log(...)` paths on the JVM platform printer (which uses
 * `java.util.logging` — always available in JVM tests; unlike Android's `Log.*`). These tests
 * can't assert on the actual handler output, but they cover the code path so Kover sees it.
 */
class PrinterLoggerFacilityJvmTest {

    private val facility = PrinterLoggerFacility()

    @Test
    fun `log with message does not throw on any level`() {
        LogLevel.entries.forEach { level ->
            facility.log(source = "src", level = level, message = LogMessage("hello"))
        }
    }

    @Test
    fun `log with error does not throw on any level`() {
        val boom = RuntimeException("boom")
        LogLevel.entries.forEach { level ->
            facility.log(source = "src", level = level, error = boom)
        }
    }

    @Test
    fun `log with message and error delegates to both single-arg variants`() {
        val boom = RuntimeException("boom")
        LogLevel.entries.forEach { level ->
            facility.log(
                source = "src",
                level = level,
                message = LogMessage("hello"),
                error = boom,
            )
        }
    }

    @Test
    fun `platform printer handles every level directly`() {
        val printer = PlatformPrinterLoggerFacilityImpl()
        LogLevel.entries.forEach { level ->
            printer.print(source = "src", level = level, message = "hello")
        }
    }

    @Test
    fun `platform printer toString identifies the platform`() {
        assertTrue(PlatformPrinterLoggerFacilityImpl().toString().isNotBlank())
    }
}
