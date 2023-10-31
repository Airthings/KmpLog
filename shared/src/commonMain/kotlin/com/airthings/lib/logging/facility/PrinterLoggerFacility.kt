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

import com.airthings.lib.logging.LogLevel
import com.airthings.lib.logging.LogMessage
import com.airthings.lib.logging.LoggerFacility

/**
 * A [LoggerFacility] that prints log messages to standard output using a platform-specific method.
 */
class PrinterLoggerFacility : LoggerFacility {
    private val facility: PlatformPrinterLoggerFacility = PlatformPrinterLoggerFacilityImpl()

    override fun isEnabled(): Boolean = true

    override fun log(source: String, level: LogLevel, message: LogMessage) {
        facility.print(
            source = source,
            level = level,
            message = format(
                level = level,
                message = message,
            ),
        )
    }

    override fun log(source: String, level: LogLevel, error: Throwable) {
        facility.print(
            source = source,
            level = level,
            message = format(
                level = level,
                error = error,
            ),
        )
    }

    override fun toString(): String = "PrinterLoggerFacility($facility)"

    companion object {
        /**
         * Returns the default formatted log message for [PrinterLoggerFacility].
         *
         * @param level The level at which to log the error.
         * @param message The message's details.
         */
        fun format(
            level: LogLevel,
            message: LogMessage,
        ): String = "${level.emoticon} $level: $message"

        /**
         * Returns the default formatted log error for [PrinterLoggerFacility].
         *
         * @param level The level at which to log the error.
         * @param error The error's details.
         */
        fun format(
            level: LogLevel,
            error: Throwable,
        ): String = "${level.emoticon} $level: ${error.stackTraceToString()}"
    }
}

/**
 * Defines the contract to [print] a log message to standard output.
 */
interface PlatformPrinterLoggerFacility {
    /**
     * Prints a [message] to standard output.
     *
     * @param source The class or function wanting to log the message.
     * @param level The level at which to log the message.
     * @param message The message to log.
     */
    fun print(source: String, level: LogLevel, message: String)
}

/**
 * Expect declaration for a [PlatformPrinterLoggerFacility].
 */
expect class PlatformPrinterLoggerFacilityImpl() : PlatformPrinterLoggerFacility
