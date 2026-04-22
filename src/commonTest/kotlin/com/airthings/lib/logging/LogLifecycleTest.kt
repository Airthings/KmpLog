package com.airthings.lib.logging

import kotlin.test.Test
import kotlin.test.assertEquals

class LogLifecycleTest {

    @Test
    fun `all five lifecycle events exist`() {
        assertEquals(
            setOf(
                LogLifecycle.CREATED,
                LogLifecycle.PAUSED,
                LogLifecycle.RESUMED,
                LogLifecycle.FINISHED,
                LogLifecycle.DESTROYED,
            ),
            LogLifecycle.entries.toSet(),
        )
    }

    @Test
    fun `toString uses the lowercase event label`() {
        assertEquals("created", LogLifecycle.CREATED.toString())
        assertEquals("paused", LogLifecycle.PAUSED.toString())
        assertEquals("resumed", LogLifecycle.RESUMED.toString())
        assertEquals("finished", LogLifecycle.FINISHED.toString())
        assertEquals("destroyed", LogLifecycle.DESTROYED.toString())
    }

    @Test
    fun `format without decoration uppercases the event`() {
        assertEquals("RESUMED", LogLifecycle.format(LogLifecycle.RESUMED))
    }

    @Test
    fun `format with default decoration applies prefix and uppercases`() {
        val decoration = LogDecoration()

        val result = LogLifecycle.format(LogLifecycle.CREATED, decoration)

        assertEquals("-- LIFECYCLE CHANGED TO: CREATED", result)
    }

    @Test
    fun `format respects custom prefix suffix and uppercase flag`() {
        val decoration = LogDecoration(prefix = "<<", suffix = ">>", uppercase = false)

        val result = LogLifecycle.format(LogLifecycle.PAUSED, decoration)

        assertEquals("<<paused>>", result)
    }

    @Test
    fun `format with decoration having null prefix and suffix just renders the event`() {
        val decoration = LogDecoration(prefix = null, suffix = null, uppercase = true)

        val result = LogLifecycle.format(LogLifecycle.DESTROYED, decoration)

        assertEquals("DESTROYED", result)
    }

    @Test
    fun `format with decoration having empty prefix and suffix just renders the event`() {
        val decoration = LogDecoration(prefix = "", suffix = "", uppercase = false)

        val result = LogLifecycle.format(LogLifecycle.FINISHED, decoration)

        assertEquals("finished", result)
    }
}
