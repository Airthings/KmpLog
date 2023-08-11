/*
 * Copyright 2023 Airthings ASA. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the “Software”), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.airthings.lib.logging.facility

import com.airthings.lib.logging.DefaultLoggerProperties
import com.airthings.lib.logging.LogLevel
import com.airthings.lib.logging.LogMessage
import com.airthings.lib.logging.LoggerFacility

/**
 * A [LoggerFacility] that sends log messages to Firebase.
 *
 * In order for this logger facility to work, you must setup Firebase in your application by following the
 * official instructions:
 *
 * - [iOS instructions](https://rdr.to/BLgNPxcAcvK)
 * - [Android instructions](https://rdr.to/jla25mQj0V)
 *
 * @param facility An implementation of [PlatformFirebaseLoggerFacility].
 * @param minimumLogLevel Only log messages and errors that are equal or greater than this level.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class FirebaseLoggerFacility constructor(
    private val facility: PlatformFirebaseLoggerFacility,
    private val minimumLogLevel: LogLevel
) : LoggerFacility {
    /**
     * Returns a [FirebaseLoggerFacility] instance that sends [WARNING][LogLevel.WARNING],
     * [ERROR][LogLevel.ERROR] and [CRASH][LogLevel.CRASH] logs to Firebase.
     *
     * If you're like to log also [INFO][LogLevel.INFO], then use the main constructor.
     */
    constructor(facility: PlatformFirebaseLoggerFacility) : this(facility, LogLevel.WARNING)

    private val properties = LinkedHashMap<String, Any?>(INITIAL_SET_SIZE)

    override fun toString(): String = "FirebaseLoggerFacility($facility)"

    override fun isEnabled(): Boolean = true

    override fun log(source: String, level: LogLevel, message: LogMessage) {
        withLogLevel(level) {
            facility.log(source, level, PrinterLoggerFacility.format(level, message), it)
        }
    }

    override fun log(source: String, level: LogLevel, error: Throwable) {
        withLogLevel(level) {
            facility.log(source, level, error, it)
        }
    }

    /**
     * Sets the user identity that will be appended to the logs.
     *
     * If the value is null, then the user identity is cleared.
     *
     * @param userId The user identity as a String.
     */
    fun setUserId(userId: String?) {
        val normalizedUserId = userId?.trim()
        if (normalizedUserId.isNullOrEmpty()) {
            facility.clearUserId()
        } else {
            facility.setUserId(normalizedUserId)
        }
    }

    /**
     * Adds one or more [logger properties][DefaultLoggerProperties] that will be appended to the logs.
     * If a logger property with the same key already exists, it'll be overridden.
     *
     * Note: properties are logged globally and not only for a specific log message. So once a property is added,
     * it'll be appended to each log message occurring afterwards until the property is [removed][removeProperties].
     *
     * @param map The default properties to add.
     */
    fun addProperties(map: DefaultLoggerProperties) {
        properties.putAll(map)
    }

    /**
     * Removes previously-added [logger properties][DefaultLoggerProperties] so that they won't be
     * appended to the logs.
     *
     * @param keys The default properties keys to remove.
     */
    fun removeProperties(keys: List<String>) {
        keys.forEach(properties::remove)
    }

    /**
     * Removes all previously-added [logger properties][DefaultLoggerProperties] so that they won't be
     * appended to the logs.
     */
    fun clearProperties() {
        properties.clear()
    }

    private fun withLogLevel(level: LogLevel, action: (DefaultLoggerProperties) -> Unit) {
        if (level.value >= minimumLogLevel.value) {
            LinkedHashMap<String, Any?>(properties.size + 1).apply {
                putAll(properties)
                action(this)
            }
        }
    }

    companion object {
        private const val INITIAL_SET_SIZE = 10
    }
}

/**
 * Defines a contract to log a message and an error to Firebase.
 *
 * The implementation of this interface is platform-specific and isn't in the shared module.
 */
interface PlatformFirebaseLoggerFacility {
    /**
     * Logs a [message] to Firebase.
     *
     * @param source The class or function wanting to log the message.
     * @param level The level at which to log the message.
     * @param message The message to log.
     * @param properties Additional properties to append to the log.
     */
    fun log(
        source: String,
        level: LogLevel,
        message: String,
        properties: DefaultLoggerProperties
    )

    /**
     * Logs an [error] to Firebase.
     *
     * @param source The class or function wanting to log the message.
     * @param level The level at which to log the error.
     * @param error The error details.
     * @param properties Additional properties to append to the log.
     */
    fun log(
        source: String,
        level: LogLevel,
        error: Throwable,
        properties: DefaultLoggerProperties
    )

    /**
     * Set the [userId] so that we can query for the userId in Firebase.
     *
     * @param userId The id of the user.
     */
    fun setUserId(userId: String)

    /**
     * Clear the userId in Firebase.
     */
    fun clearUserId()
}
