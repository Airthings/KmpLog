package com.airthings.lib.logging

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LogArgTest {

    @Test
    fun `label is lowercased and trimmed`() {
        val arg = LogArg(label = "  STATUS  ", value = 200)
        assertEquals("status", arg.label)
    }

    @Test
    fun `dashes in the label are replaced with underscores`() {
        val arg = LogArg(label = "device-type", value = "wave")
        assertEquals("device_type", arg.label)
    }

    @Test
    fun `toString delegates to LogArgument format`() {
        val arg = LogArg("status", 200)
        assertEquals("[status=200]", arg.toString())
    }

    @Test
    fun `equality is based on label and value`() {
        assertEquals(LogArg("a", 1), LogArg("a", 1))
        assertEquals(LogArg("A", 1), LogArg("a", 1)) // labels normalised equally.
        assertNotEquals(LogArg("a", 1), LogArg("a", 2))
        assertNotEquals(LogArg("a", 1), LogArg("b", 1))
    }

    @Test
    fun `hashCode is stable across equal instances`() {
        assertEquals(LogArg("a", 1).hashCode(), LogArg("a", 1).hashCode())
    }

    @Test
    fun `equals rejects non-LogArg values`() {
        assertNotEquals<Any?>(LogArg("a", 1), "a=1")
        assertNotEquals<Any?>(LogArg("a", 1), null)
    }
}
