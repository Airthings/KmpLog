package com.airthings.lib.logging

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDateTime

class LogUtilTest {

    @Test
    fun `padded pads single digits with a leading zero by default`() {
        assertEquals("00", 0.padded())
        assertEquals("01", 1.padded())
        assertEquals("09", 9.padded())
        assertEquals("10", 10.padded())
        assertEquals("99", 99.padded())
    }

    @Test
    fun `padded leaves values at or above the target length unchanged`() {
        assertEquals("100", 100.padded())
        assertEquals("2024", 2024.padded(length = 2))
    }

    @Test
    fun `padded honours a custom length`() {
        assertEquals("0003", 3.padded(length = 4))
        assertEquals("0099", 99.padded(length = 4))
        assertEquals("2024", 2024.padded(length = 4))
    }

    @Test
    fun `dateStamp formats an explicit LocalDateTime as YYYY-MM-DD`() {
        val date = LocalDateTime(year = 2024, monthNumber = 3, dayOfMonth = 5, hour = 0, minute = 0)
        assertEquals("2024-03-05", dateStamp(date))
    }

    @Test
    fun `dateStamp zero-pads single-digit months and days`() {
        val date = LocalDateTime(year = 2024, monthNumber = 1, dayOfMonth = 9, hour = 12, minute = 34)
        assertEquals("2024-01-09", dateStamp(date))
    }

    @Test
    fun `dateStamp falls back to now when the input is null`() {
        val result = dateStamp(value = null)
        // We can't assert the exact date, but we can assert the shape.
        assertTrue(
            result.matches(Regex("""\d{4}-\d{2}-\d{2}""")),
            "Expected YYYY-MM-DD, got '$result'",
        )
    }

    @Test
    fun `utc returns a value close to now`() {
        // Sanity check — just prove utc() produces a parseable LocalDateTime.
        val now = utc()
        assertTrue(now.year in 2020..2100, "Unexpected year: ${now.year}")
        assertTrue(now.monthNumber in 1..12)
        assertTrue(now.dayOfMonth in 1..31)
    }
}
