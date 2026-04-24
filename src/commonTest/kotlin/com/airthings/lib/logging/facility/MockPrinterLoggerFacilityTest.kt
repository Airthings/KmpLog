package com.airthings.lib.logging.facility

import com.airthings.lib.logging.LogLevel
import com.airthings.lib.logging.LogMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MockPrinterLoggerFacilityTest {

    private val facility = MockPrinterLoggerFacility()

    @Test
    fun `isEnabled returns true`() {
        assertTrue(facility.isEnabled())
    }

    @Test
    fun `log with message is a no-op that doesn't throw`() {
        facility.log(source = "s", level = LogLevel.INFO, message = LogMessage("hello"))
    }

    @Test
    fun `log with error is a no-op that doesn't throw`() {
        facility.log(source = "s", level = LogLevel.ERROR, error = RuntimeException("boom"))
    }

    @Test
    fun `log with message and error is a no-op that doesn't throw`() {
        facility.log(
            source = "s",
            level = LogLevel.WARNING,
            message = LogMessage("hello"),
            error = RuntimeException("boom"),
        )
    }

    @Test
    fun `toString is the class name`() {
        assertEquals("MockPrinterLoggerFacility", facility.toString())
    }
}
