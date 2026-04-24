package com.airthings.lib.logging.facility

import com.airthings.lib.logging.DefaultLoggerProperties
import com.airthings.lib.logging.LogLevel
import com.airthings.lib.logging.LogMessage
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class FirebaseLoggerFacilityTest {

    private lateinit var fake: FakePlatformFirebaseLoggerFacility

    @BeforeTest
    fun setUp() {
        fake = FakePlatformFirebaseLoggerFacility()
    }

    @Test
    fun `isEnabled is always true`() {
        val facility = FirebaseLoggerFacility(fake, LogLevel.INFO)
        assertTrue(facility.isEnabled())
    }

    @Test
    fun `toString includes the inner platform facility`() {
        val facility = FirebaseLoggerFacility(fake, LogLevel.INFO)
        assertContains(facility.toString(), "FirebaseLoggerFacility(")
    }

    @Test
    fun `secondary constructor defaults to WARNING minimum level`() {
        val facility = FirebaseLoggerFacility(fake)

        facility.log(source = "src", level = LogLevel.INFO, message = LogMessage("info"))
        facility.log(source = "src", level = LogLevel.WARNING, message = LogMessage("warn"))

        assertEquals(1, fake.messageCalls.size, "INFO should be filtered out at default WARNING")
        assertContains(fake.messageCalls.first().message, "WARNING: warn")
    }

    @Test
    fun `log with message forwards to the platform facility with formatted text`() {
        val facility = FirebaseLoggerFacility(fake, LogLevel.INFO)

        facility.log(source = "src", level = LogLevel.ERROR, message = LogMessage("boom"))

        val call = fake.messageCalls.single()
        assertEquals("src", call.source)
        assertEquals(LogLevel.ERROR, call.level)
        assertContains(call.message, "ERROR: boom")
    }

    @Test
    fun `log with error forwards raw throwable to the platform facility`() {
        val facility = FirebaseLoggerFacility(fake, LogLevel.INFO)
        val error = RuntimeException("oops")

        facility.log(source = "src", level = LogLevel.ERROR, error = error)

        val call = fake.errorCalls.single()
        assertEquals("src", call.source)
        assertEquals(LogLevel.ERROR, call.level)
        assertSame(error, call.error)
    }

    @Test
    fun `log with message and error forwards both in sequence`() {
        val facility = FirebaseLoggerFacility(fake, LogLevel.INFO)
        val error = RuntimeException("oops")

        facility.log(source = "src", level = LogLevel.ERROR, message = LogMessage("hi"), error = error)

        assertEquals(1, fake.messageCalls.size)
        assertEquals(1, fake.errorCalls.size)
        assertSame(error, fake.errorCalls.single().error)
    }

    @Test
    fun `log filters entries below the minimum level`() {
        val facility = FirebaseLoggerFacility(fake, LogLevel.WARNING)

        facility.log(source = "src", level = LogLevel.INFO, message = LogMessage("quiet"))
        facility.log(source = "src", level = LogLevel.DEBUG, message = LogMessage("quieter"))

        assertEquals(0, fake.messageCalls.size)
        assertEquals(0, fake.errorCalls.size)
    }

    @Test
    fun `properties added via addProperties flow into subsequent log calls`() {
        val facility = FirebaseLoggerFacility(fake, LogLevel.INFO)
        facility.addProperties(mapOf("device_id" to "abc", "build" to 42))

        facility.log(source = "src", level = LogLevel.INFO, message = LogMessage("hi"))

        val call = fake.messageCalls.single()
        assertEquals("abc", call.properties["device_id"])
        assertEquals(42, call.properties["build"])
    }

    @Test
    fun `removeProperties drops specific keys`() {
        val facility = FirebaseLoggerFacility(fake, LogLevel.INFO)
        facility.addProperties(mapOf("a" to 1, "b" to 2, "c" to 3))

        facility.removeProperties(listOf("b"))
        facility.log(source = "src", level = LogLevel.INFO, message = LogMessage("hi"))

        val props = fake.messageCalls.single().properties
        assertEquals(1, props["a"])
        assertEquals(3, props["c"])
        assertNull(props["b"])
    }

    @Test
    fun `clearProperties empties the property bag`() {
        val facility = FirebaseLoggerFacility(fake, LogLevel.INFO)
        facility.addProperties(mapOf("a" to 1))

        facility.clearProperties()
        facility.log(source = "src", level = LogLevel.INFO, message = LogMessage("hi"))

        assertTrue(fake.messageCalls.single().properties.isEmpty())
    }

    @Test
    fun `setUserId forwards a trimmed non-blank id`() {
        val facility = FirebaseLoggerFacility(fake, LogLevel.INFO)

        facility.setUserId("  user-42  ")

        assertEquals("user-42", fake.lastUserId)
        assertEquals(0, fake.clearUserIdCalls)
    }

    @Test
    fun `setUserId with null clears the user id`() {
        val facility = FirebaseLoggerFacility(fake, LogLevel.INFO)

        facility.setUserId(null)

        assertEquals(1, fake.clearUserIdCalls)
    }

    @Test
    fun `setUserId with blank clears the user id`() {
        val facility = FirebaseLoggerFacility(fake, LogLevel.INFO)

        facility.setUserId("   ")

        assertEquals(1, fake.clearUserIdCalls)
    }
}

private class FakePlatformFirebaseLoggerFacility : PlatformFirebaseLoggerFacility {
    data class MessageCall(
        val source: String,
        val level: LogLevel,
        val message: String,
        val properties: DefaultLoggerProperties,
    )

    data class ErrorCall(
        val source: String,
        val level: LogLevel,
        val error: Throwable,
        val properties: DefaultLoggerProperties,
    )

    val messageCalls = mutableListOf<MessageCall>()
    val errorCalls = mutableListOf<ErrorCall>()
    var lastUserId: String? = null
    var clearUserIdCalls: Int = 0

    override fun log(
        source: String,
        level: LogLevel,
        message: String,
        properties: DefaultLoggerProperties,
    ) {
        messageCalls += MessageCall(source, level, message, properties.toMap())
    }

    override fun log(
        source: String,
        level: LogLevel,
        error: Throwable,
        properties: DefaultLoggerProperties,
    ) {
        errorCalls += ErrorCall(source, level, error, properties.toMap())
    }

    override fun setUserId(userId: String) {
        lastUserId = userId
    }

    override fun clearUserId() {
        clearUserIdCalls++
    }
}
