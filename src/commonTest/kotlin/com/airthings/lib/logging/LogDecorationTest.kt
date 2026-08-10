package com.airthings.lib.logging

import kotlin.test.Test
import kotlin.test.assertEquals

class LogDecorationTest {

    @Test
    fun `default construction uses the documented defaults`() {
        val decoration = LogDecoration()
        assertEquals("-- Lifecycle changed to: ", decoration.prefix)
        assertEquals(null, decoration.suffix)
        assertEquals(true, decoration.uppercase)
    }

    @Test
    fun `custom values are preserved`() {
        val decoration = LogDecoration(prefix = "<<", suffix = ">>", uppercase = false)
        assertEquals("<<", decoration.prefix)
        assertEquals(">>", decoration.suffix)
        assertEquals(false, decoration.uppercase)
    }

    @Test
    fun `null prefix and suffix are allowed`() {
        val decoration = LogDecoration(prefix = null, suffix = null)
        assertEquals(null, decoration.prefix)
        assertEquals(null, decoration.suffix)
    }

    @Test
    fun `data class equality is structural`() {
        assertEquals(LogDecoration(), LogDecoration())
        assertEquals(
            LogDecoration(prefix = "X", suffix = "Y", uppercase = false),
            LogDecoration(prefix = "X", suffix = "Y", uppercase = false),
        )
    }
}
