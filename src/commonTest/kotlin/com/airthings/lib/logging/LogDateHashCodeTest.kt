package com.airthings.lib.logging

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Basic hashCode coverage for [LogDate]. Full class coverage lives in `LogDateTest` on the
 * `fix/logdate-after-precedence` branch / PR #40 — these tests pin the hashCode-specific
 * invariants that hold on the current (pre-fix) code.
 */
class LogDateHashCodeTest {

    @Test
    fun `hashCode is equal for equal instances`() {
        assertEquals(LogDate(2024, 3, 5).hashCode(), LogDate(2024, 3, 5).hashCode())
    }

    @Test
    fun `hashCode ignores separator for realistic years`() {
        assertEquals(
            LogDate(2024, 3, 5, separator = '-').hashCode(),
            LogDate(2024, 3, 5, separator = ':').hashCode(),
        )
    }

    @Test
    fun `hashCode differs for different days`() {
        assertNotEquals(LogDate(2024, 3, 5).hashCode(), LogDate(2024, 3, 6).hashCode())
    }

    @Test
    fun `hashCode differs for different months`() {
        assertNotEquals(LogDate(2024, 3, 5).hashCode(), LogDate(2024, 4, 5).hashCode())
    }

    @Test
    fun `hashCode differs for different years`() {
        assertNotEquals(LogDate(2024, 3, 5).hashCode(), LogDate(2025, 3, 5).hashCode())
    }
}
