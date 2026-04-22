package com.airthings.lib.logging.message

import com.airthings.lib.logging.LogArg
import com.airthings.lib.logging.LogArgument
import kotlin.test.Test
import kotlin.test.assertEquals

class NetworkLogMessageTest {

    @Test
    fun `method is uppercased and path preserved`() {
        val message = NetworkLogMessage(method = "get", path = "/v1/users", message = "Request")

        assertEquals("Request", message.message)
        val first = message.args[0]
        val second = message.args[1]
        assertEquals("method" to "GET", first.label to first.value)
        assertEquals("path" to "/v1/users", second.label to second.value)
    }

    @Test
    fun `additional args are appended after method and path`() {
        val extra: List<LogArgument> = listOf(LogArg("status", 200), LogArg("duration_ms", 42))
        val message = NetworkLogMessage(
            method = "POST",
            path = "/v1/x",
            message = "Response",
            args = extra,
        )

        assertEquals(4, message.args.size)
        assertEquals("method", message.args[0].label)
        assertEquals("POST", message.args[0].value)
        assertEquals("path", message.args[1].label)
        assertEquals("status", message.args[2].label)
        assertEquals(200, message.args[2].value)
        assertEquals("duration_ms", message.args[3].label)
        assertEquals(42, message.args[3].value)
    }

    @Test
    fun `secondary constructor leaves extra args empty`() {
        val message = NetworkLogMessage(method = "DELETE", path = "/v1/x/1", message = "Removed")

        assertEquals(2, message.args.size)
        assertEquals("method", message.args[0].label)
        assertEquals("DELETE", message.args[0].value)
        assertEquals("path", message.args[1].label)
    }

    @Test
    fun `toString formats using the parent LogMessage format`() {
        val message = NetworkLogMessage(method = "get", path = "/v1/users", message = "Request")

        assertEquals(
            "Request [method=\"GET\"] [path=\"/v1/users\"]",
            message.toString(),
        )
    }
}
