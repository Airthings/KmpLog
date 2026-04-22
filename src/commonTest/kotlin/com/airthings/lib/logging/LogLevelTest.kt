package com.airthings.lib.logging

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LogLevelTest {

    @Test
    fun `all five levels are defined`() {
        assertEquals(
            setOf(LogLevel.DEBUG, LogLevel.INFO, LogLevel.WARNING, LogLevel.ERROR, LogLevel.CRASH),
            LogLevel.entries.toSet(),
        )
    }

    @Test
    fun `values are monotonically increasing by severity`() {
        val bySeverity = LogLevel.entries.sortedBy { it.value }
        assertEquals(
            listOf(LogLevel.DEBUG, LogLevel.INFO, LogLevel.WARNING, LogLevel.ERROR, LogLevel.CRASH),
            bySeverity,
        )
    }

    @Test
    fun `DEBUG value is below INFO so default prod filters drop it`() {
        assertTrue(LogLevel.DEBUG.value < LogLevel.INFO.value)
    }

    @Test
    fun `labels match the level name in lowercase`() {
        assertEquals("debug", LogLevel.DEBUG.label)
        assertEquals("info", LogLevel.INFO.label)
        assertEquals("warning", LogLevel.WARNING.label)
        assertEquals("error", LogLevel.ERROR.label)
        assertEquals("crash", LogLevel.CRASH.label)
    }

    @Test
    fun `toString uppercases the label`() {
        assertEquals("DEBUG", LogLevel.DEBUG.toString())
        assertEquals("INFO", LogLevel.INFO.toString())
        assertEquals("WARNING", LogLevel.WARNING.toString())
        assertEquals("ERROR", LogLevel.ERROR.toString())
        assertEquals("CRASH", LogLevel.CRASH.toString())
    }

    @Test
    fun `every level has a non-blank emoticon and they are all distinct`() {
        val emoticons = LogLevel.entries.map { it.emoticon }
        assertTrue(emoticons.all { it.isNotBlank() })
        assertEquals(emoticons.size, emoticons.toSet().size, "Emoticons must be unique per level.")
    }
}
