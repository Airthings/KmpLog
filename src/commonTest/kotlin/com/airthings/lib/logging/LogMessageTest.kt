package com.airthings.lib.logging

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LogMessageTest {

    @Test
    fun `single-arg constructor yields empty args`() {
        val message = LogMessage("hello")
        assertEquals("hello", message.message)
        assertTrue(message.args.isEmpty())
    }

    @Test
    fun `map constructor converts into a list of LogArgs preserving order`() {
        val message = LogMessage("hello", mapOf("a" to 1, "b" to 2))

        assertEquals("hello", message.message)
        assertEquals(listOf("a" to 1, "b" to 2), message.args.map { it.label to it.value })
    }

    @Test
    fun `vararg constructor converts pairs into LogArgs`() {
        val message = LogMessage("hello", "a" to 1, "b" to "two")

        assertEquals("hello", message.message)
        assertEquals(listOf("a" to 1, "b" to "two"), message.args.map { it.label to it.value })
    }

    @Test
    fun `primary constructor accepts a raw LogArgument list`() {
        val args = listOf<LogArgument>(LogArg("x", 42))
        val message = LogMessage("hello", args)

        assertEquals("hello", message.message)
        assertEquals(args, message.args)
    }

    @Test
    fun `toString formats the message followed by each argument`() {
        val message = LogMessage("Sending request.", mapOf("status" to 200, "path" to "/v1/x"))

        // Each LogArg renders as [label=value]; value rendering is covered in LogArgumentTest.
        assertEquals(
            "Sending request. [status=200] [path=\"/v1/x\"]",
            message.toString(),
        )
    }

    @Test
    fun `format trims the leading and trailing whitespace off the message`() {
        val formatted = LogMessage.format(message = "  hello  ", args = emptyList())
        assertEquals("hello", formatted)
    }

    @Test
    fun `format handles empty args`() {
        assertEquals("hello", LogMessage.format("hello", emptyList()))
    }
}
