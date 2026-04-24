package com.airthings.lib.logging

import com.airthings.lib.logging.facility.MockPrinterLoggerFacility
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Covers the [LoggerFacility.Companion] registry — register / deregister / get / clear /
 * `facilities` / `enabledFacilities`.
 */
class LoggerFacilityRegistryTest {

    @BeforeTest
    fun setUp() {
        LoggerFacility.clear()
    }

    @AfterTest
    fun tearDown() {
        LoggerFacility.clear()
    }

    @Test
    fun `register adds a facility retrievable by name`() {
        val facility = MockPrinterLoggerFacility()

        LoggerFacility.register("mock", facility)

        assertSame(facility, LoggerFacility.get<MockPrinterLoggerFacility>("mock"))
        assertTrue(LoggerFacility.facilities.contains(facility))
    }

    @Test
    fun `register currently keeps the first facility for a duplicate name`() {
        // ⚠ BUG: LoggerFacility.register's docstring says "the previous one will be replaced"
        // but the code actually keeps the first registration. This test pins the current (buggy)
        // behavior to prevent accidental changes. The companion test below documents the
        // documented semantics and is @Ignored until the fix lands.
        // See: bug_loggerfacility_register_doc_mismatch.md in the KmpLog memory folder.
        val first = MockPrinterLoggerFacility()
        val second = MockPrinterLoggerFacility()

        LoggerFacility.register("mock", first)
        LoggerFacility.register("mock", second)

        assertSame(first, LoggerFacility.get<MockPrinterLoggerFacility>("mock"))
    }

    @Ignore
    @Test
    fun `register replaces an existing facility for the same name as documented`() {
        // Un-ignore and remove the companion test above when the bug fix lands.
        val first = MockPrinterLoggerFacility()
        val second = MockPrinterLoggerFacility()

        LoggerFacility.register("mock", first)
        LoggerFacility.register("mock", second)

        assertSame(second, LoggerFacility.get<MockPrinterLoggerFacility>("mock"))
    }

    @Test
    fun `deregister removes a registered facility`() {
        val facility = MockPrinterLoggerFacility()
        LoggerFacility.register("mock", facility)

        LoggerFacility.deregister("mock")

        assertNull(LoggerFacility.get<MockPrinterLoggerFacility>("mock"))
        assertFalse(LoggerFacility.facilities.contains(facility))
    }

    @Test
    fun `deregister with unknown name is a no-op`() {
        LoggerFacility.deregister("does-not-exist")
        assertTrue(LoggerFacility.facilities.isEmpty())
    }

    @Test
    fun `clear removes all registered facilities`() {
        LoggerFacility.register("a", MockPrinterLoggerFacility())
        LoggerFacility.register("b", MockPrinterLoggerFacility())

        LoggerFacility.clear()

        assertTrue(LoggerFacility.facilities.isEmpty())
    }

    @Test
    fun `enabledFacilities includes only those whose isEnabled is true`() {
        LoggerFacility.register("on", AlwaysEnabledFake(enabled = true))
        LoggerFacility.register("off", AlwaysEnabledFake(enabled = false))

        val enabled = LoggerFacility.enabledFacilities
        assertEquals(1, enabled.size)
        assertTrue(enabled.all { (it as AlwaysEnabledFake).enabled })
    }

    @Test
    fun `facilities returns every registered facility regardless of enabled flag`() {
        LoggerFacility.register("on", AlwaysEnabledFake(enabled = true))
        LoggerFacility.register("off", AlwaysEnabledFake(enabled = false))

        assertEquals(2, LoggerFacility.facilities.size)
    }
}

private class AlwaysEnabledFake(val enabled: Boolean) : LoggerFacility {
    override fun isEnabled(): Boolean = enabled
    override fun log(
        source: String,
        level: LogLevel,
        message: LogMessage,
    ) = Unit
    override fun log(
        source: String,
        level: LogLevel,
        error: Throwable,
    ) = Unit
    override fun log(
        source: String,
        level: LogLevel,
        message: LogMessage,
        error: Throwable,
    ) = Unit
}
