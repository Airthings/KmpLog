package com.airthings.lib.logging

import kotlin.test.Test
import kotlin.test.assertEquals

class LogArgumentTest {

    @Test
    fun `format wraps label and value in brackets`() {
        assertEquals("[status=200]", LogArgument.format("status", 200))
    }

    @Test
    fun `formatValue renders null as explicit marker`() {
        assertEquals("(null)", LogArgument.formatValue(null))
    }

    @Test
    fun `formatValue quotes char sequences`() {
        assertEquals("\"hello\"", LogArgument.formatValue("hello"))
        assertEquals("\"\"", LogArgument.formatValue(""))
    }

    @Test
    fun `formatValue renders scalars via toString`() {
        assertEquals("42", LogArgument.formatValue(42))
        assertEquals("3.14", LogArgument.formatValue(3.14))
        assertEquals("true", LogArgument.formatValue(true))
    }

    @Test
    fun `formatMap renders entries separated by comma`() {
        val result = LogArgument.formatValue(mapOf("a" to 1, "b" to "two"))
        assertEquals("{a: 1, b: \"two\"}", result)
    }

    @Test
    fun `formatMap renders empty map as empty braces`() {
        assertEquals("{}", LogArgument.formatValue(emptyMap<String, Any?>()))
    }

    @Test
    fun `formatArray labels as Array with values`() {
        val arr: Array<Any?> = arrayOf(1, "two", null)
        assertEquals("Array(1, \"two\", (null))", LogArgument.formatValue(arr))
    }

    @Test
    fun `formatList labels as List with values`() {
        assertEquals("List(1, 2, 3)", LogArgument.formatValue(listOf(1, 2, 3)))
    }

    @Test
    fun `formatValue for Set labels it as Collection`() {
        val formatted = LogArgument.formatValue(setOf("x"))
        assertEquals("Collection(\"x\")", formatted)
    }

    @Test
    fun `formatValue for a pure Iterable labels it as Iterable`() {
        val iterable: Iterable<Int> = Iterable { listOf(1, 2).iterator() }
        assertEquals("Iterable(1, 2)", LogArgument.formatValue(iterable))
    }

    @Test
    fun `formatValue recurses through nested containers`() {
        val value = mapOf("list" to listOf("a", mapOf("k" to "v")))
        assertEquals(
            "{list: List(\"a\", {k: \"v\"})}",
            LogArgument.formatValue(value),
        )
    }
}
