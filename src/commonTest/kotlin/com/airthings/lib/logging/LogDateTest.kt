package com.airthings.lib.logging

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDateTime

class LogDateTest {

    // region constructor + data-class behavior

    @Test
    fun `constructor stores the date parts`() {
        val date = LogDate(year = 2024, month = 3, day = 5)
        assertEquals(2024, date.year)
        assertEquals(3, date.month)
        assertEquals(5, date.day)
        assertEquals(LogDate.SEPARATOR, date.separator)
    }

    @Test
    fun `constructor accepts a custom separator`() {
        val date = LogDate(year = 2024, month = 3, day = 5, separator = ':')
        assertEquals(':', date.separator)
    }

    @Test
    fun `constructor from LocalDateTime copies the date parts`() {
        val localDateTime = LocalDateTime(year = 2024, monthNumber = 3, dayOfMonth = 5, hour = 12, minute = 0)
        val date = LogDate(localDateTime)
        assertEquals(2024, date.year)
        assertEquals(3, date.month)
        assertEquals(5, date.day)
    }

    @Test
    fun `constructor rejects months outside 1 to 12`() {
        assertFailsWith<IllegalArgumentException> { LogDate(year = 2024, month = 0, day = 1) }
        assertFailsWith<IllegalArgumentException> { LogDate(year = 2024, month = 13, day = 1) }
    }

    @Test
    fun `constructor rejects days outside 1 to 31`() {
        assertFailsWith<IllegalArgumentException> { LogDate(year = 2024, month = 5, day = 0) }
        assertFailsWith<IllegalArgumentException> { LogDate(year = 2024, month = 5, day = 32) }
    }

    @Test
    fun `equality ignores the separator`() {
        assertEquals(
            LogDate(2024, 3, 5, separator = '-'),
            LogDate(2024, 3, 5, separator = ':'),
        )
    }

    @Test
    fun `equality compares year month and day`() {
        assertNotEquals(LogDate(2024, 3, 5), LogDate(2024, 3, 6))
        assertNotEquals(LogDate(2024, 3, 5), LogDate(2024, 4, 5))
        assertNotEquals(LogDate(2024, 3, 5), LogDate(2025, 3, 5))
    }

    @Test
    fun `equals rejects non-LogDate values`() {
        assertNotEquals<Any?>(LogDate(2024, 3, 5), "2024-03-05")
        assertNotEquals<Any?>(LogDate(2024, 3, 5), null)
    }

    // endregion

    // region hashCode

    @Test
    fun `hashCode is stable across equal instances`() {
        assertEquals(LogDate(2024, 3, 5).hashCode(), LogDate(2024, 3, 5).hashCode())
    }

    @Test
    fun `hashCode ignores the separator just like equals`() {
        assertEquals(
            LogDate(2024, 3, 5, separator = '-').hashCode(),
            LogDate(2024, 3, 5, separator = ':').hashCode(),
        )
    }

    @Test
    fun `hashCode differs for different dates`() {
        assertNotEquals(LogDate(2024, 3, 5).hashCode(), LogDate(2024, 3, 6).hashCode())
    }

    // endregion

    // region toString

    @Test
    fun `toString uses the default separator`() {
        assertEquals("2024-03-05", LogDate(2024, 3, 5).toString())
    }

    @Test
    fun `toString with a null separator produces a compact digit string`() {
        assertEquals("20240305", LogDate(2024, 3, 5).toString(separator = null))
    }

    @Test
    fun `toString with a custom separator renders it between parts`() {
        assertEquals("2024:03:05", LogDate(2024, 3, 5).toString(separator = ':'))
    }

    @Test
    fun `toString zero-pads single-digit months and days`() {
        assertEquals("2024-01-09", LogDate(2024, 1, 9).toString())
    }

    // endregion

    // region asLogDate

    @Test
    fun `asLogDate parses a well-formed separated date`() {
        assertEquals(LogDate(2024, 3, 5), "2024-03-05".asLogDate(separator = '-'))
    }

    @Test
    fun `asLogDate parses a well-formed compact date`() {
        assertEquals(LogDate(2024, 3, 5), "20240305".asLogDate(separator = null))
    }

    @Test
    fun `asLogDate strips a trailing file extension before parsing`() {
        assertEquals(LogDate(2024, 3, 5), "2024-03-05.log".asLogDate(separator = '-'))
        assertEquals(LogDate(2024, 3, 5), "20240305.json".asLogDate(separator = null))
    }

    @Test
    fun `asLogDate returns null for wrong length strings`() {
        assertNull("2024-03-5".asLogDate(separator = '-'))
        assertNull("202403".asLogDate(separator = null))
    }

    @Test
    fun `asLogDate returns null for non-numeric inputs`() {
        assertNull("abcd-ef-gh".asLogDate(separator = '-'))
    }

    // endregion

    // region after

    @Test
    fun `after returns true when years differ and this is later`() {
        assertTrue(LogDate(2024, 1, 1).after(LogDate(2023, 12, 31)))
    }

    @Test
    fun `after returns false when years differ and this is earlier`() {
        assertFalse(LogDate(2023, 12, 31).after(LogDate(2024, 1, 1)))
    }

    @Test
    fun `after returns false when same month but this year is earlier and day is later`() {
        assertFalse(LogDate(2023, 1, 5).after(LogDate(2024, 1, 3)))
    }

    @Test
    fun `after returns true when same year and later month`() {
        assertTrue(LogDate(2024, 5, 1).after(LogDate(2024, 3, 31)))
    }

    @Test
    fun `after returns false when same year and earlier month`() {
        assertFalse(LogDate(2024, 3, 31).after(LogDate(2024, 5, 1)))
    }

    @Test
    fun `after returns true when same year and same month and later day`() {
        assertTrue(LogDate(2024, 3, 5).after(LogDate(2024, 3, 3)))
    }

    @Test
    fun `after returns false when same year and same month and earlier day`() {
        assertFalse(LogDate(2024, 3, 3).after(LogDate(2024, 3, 5)))
    }

    @Test
    fun `after returns false for equal dates`() {
        assertFalse(LogDate(2024, 3, 5).after(LogDate(2024, 3, 5)))
    }

    // endregion

    // region ifAfter

    @Test
    fun `ifAfter returns true when the file date is from a later year`() {
        assertTrue("2025-01-01.log".ifAfter(LogDate(2024, 1, 1)))
    }

    @Test
    fun `ifAfter returns false when the file date is from an earlier year`() {
        assertFalse("2023-07-01.log".ifAfter(LogDate(2024, 3, 1)))
    }

    @Test
    fun `ifAfter with a null date always returns true for parseable strings`() {
        assertTrue("2024-03-05.log".ifAfter(date = null))
    }

    @Test
    fun `ifAfter returns false for unparseable strings`() {
        assertFalse("not-a-date.log".ifAfter(LogDate(2024, 3, 5)))
    }

    // endregion
}
