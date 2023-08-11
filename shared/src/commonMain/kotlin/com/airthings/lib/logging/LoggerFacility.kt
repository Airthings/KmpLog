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

package com.airthings.lib.logging

import co.touchlab.stately.concurrency.AtomicReference
import com.airthings.lib.logging.facility.PrinterLoggerFacility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Defines a contract for a logging implementation used by [Logger].
 *
 * For example, in a development environment you'd want to define a [PrinterLoggerFacility] which writes all log
 * messages to the screen. Whereas, in a production environment, you'd most likely want to use a logging facility
 * that sends the logs to Firebase or another 3rd party logging service.
 */
@Suppress("unused")
interface LoggerFacility {
    /**
     * Returns true if this logger facility is enabled and can log messages, false otherwise.
     */
    fun isEnabled(): Boolean

    /**
     * Logs a message with this facility.
     *
     * @param source A source tag that identifies the message.
     * @param level The level at which to log the message.
     * @param message The message's details.
     */
    fun log(source: String, level: LogLevel, message: LogMessage)

    /**
     * Logs an error with this facility.
     *
     * @param source A source tag that identifies the message.
     * @param level The level at which to log the error.
     * @param error The error's details.
     */
    fun log(source: String, level: LogLevel, error: Throwable)

    /**
     * Contains controller functions to define logging facilities.
     */
    companion object {
        private val facilitiesAtomicHolder = AtomicReference<LoggerFacilitiesMap?>(null)

        private var facilitiesMap: LoggerFacilitiesMap
            get() = facilitiesAtomicHolder.run {
                var currentMap = get()
                if (currentMap == null) {
                    currentMap = facilitiesMapCreator()
                    set(currentMap)
                }
                currentMap
            }
            set(value) {
                facilitiesAtomicHolder.set(value)
            }

        /**
         * The default coroutine scope for logging operations.
         *
         * The default (Dispatchers.Default + SupervisorJob()) uses a sane value to support most use cases.
         * If your use case is different, pass your own scope when instantiating the `Logger` instance in question.
         */
        val defaultCoroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

        /**
         * Retrieves a map of [previously-registered][register] facilities in [Logger].
         */
        val facilities: Collection<LoggerFacility>
            get() = facilitiesMap.values

        /**
         * Retrieves the map of [previously-registered][register] facilities in [Logger] which are [still enabled][isEnabled].
         */
        val enabledFacilities: Collection<LoggerFacility>
            get() = facilitiesMap.filter { it.value.isEnabled() }.values

        /**
         * Retrieves a [previously-registered][register] [LoggerFacility] instance having the provided [name] on success,
         * null otherwise.
         *
         * @param name The unique name of the [LoggerFacility] instance to get.
         */
        fun <L : LoggerFacility> get(name: String): L? = facilitiesMap[name]?.let {
            castOrNull<L>(it)
        }

        /**
         * Registers a new named [LoggerFacility] in the list of installed facilities used by [Logger].
         *
         * If there already exists a facility with the same [name], the previous one will be replaced.
         *
         * @param name The unique name to use for this [LoggerFacility] instance.
         * @param facility The [LoggerFacility] instance to register.
         */
        fun register(name: String, facility: LoggerFacility) {
            val facilities = facilitiesMap
            if (!facilities.containsKey(name)) {
                facilitiesMapCreator(1 + facilities.size).apply {
                    putAll(facilities)
                    put(name, facility)
                    facilitiesMap = this
                }
            }
        }

        /**
         * Deregisters a named [LoggerFacility] instance from the list of installed facilities used by [Logger].
         *
         * If no registered facility is found with this name, nothing happens.
         *
         * @param name The unique name of the [LoggerFacility] instance to remove.
         */
        fun deregister(name: String) {
            val facilities = facilitiesMap
            if (facilities.containsKey(name)) {
                facilitiesMapCreator(1 + facilities.size).apply {
                    putAll(facilities)
                    remove(name)
                    facilitiesMap = this
                }
            }
        }

        /**
         * Clears the list of facilities used by [Logger].
         */
        fun clear() {
            facilitiesMap = facilitiesMapCreator()
        }

        @Suppress("MagicNumber")
        private fun facilitiesMapCreator(size: Int = 5): LoggerFacilitiesMutableMap = LinkedHashMap(size)

        @Suppress("UNCHECKED_CAST")
        private fun <T> castOrNull(from: Any): T? = try {
            from as T
        } catch (ignored: ClassCastException) {
            null
        }
    }
}
