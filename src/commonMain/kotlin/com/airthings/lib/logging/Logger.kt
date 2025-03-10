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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * The main logging class.
 *
 * When the app starts, you need to configure the logger facilities by calling [LoggerFacility.register] as needed.
 *
 * Note: because of interoperability issues with default nullable parameters in KMM, methods in this class accept
 * only non-null parameters. The compatible Objective-C code created by KMM enable us to write Swift code without
 * listing all "nil" parameters.
 *
 * @param source A source tag that identifies this logger's instance.
 * @param decoration An optional decoration for the lifecycle log messages.
 * @param coroutineScope The coroutine scope used to call the log functions on the facilities.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class Logger(
    val source: String,
    val decoration: LogDecoration?,
    val coroutineScope: CoroutineScope,
) {
    /**
     * Instantiates a new [Logger] with the default [coroutineScope].
     *
     * @param source A source tag that identifies this logger's instance.
     * @param decoration An optional decoration for the lifecycle log messages.
     */
    constructor(source: String, decoration: LogDecoration?) : this(
        source = source,
        decoration = decoration,
        coroutineScope = LoggerFacility.defaultCoroutineScope,
    )

    /**
     * Instantiates a new [Logger] without a [LogDecoration] and a [CoroutineScope].
     *
     * @param source A source tag that identifies this logger's instance.
     */
    constructor(source: String) : this(
        source = source,
        decoration = null,
    )

    /**
     * Logs a message with a [LogLevel.INFO] level.
     *
     * @param message The message.
     */
    fun info(message: String) {
        info(
            message = LogMessage(message),
        )
    }

    /**
     * Logs a message with a [LogLevel.INFO] level.
     *
     * For a boilerplate-free implementation, check out the overloaded function with vararg arguments.
     *
     * @param message The message.
     * @param arguments The arguments in a map.
     *
     * @see info(message: String, vararg arguments: Pair<String, Any?>)
     */
    fun info(
        message: String,
        arguments: Map<String, Any?>,
    ) {
        info(
            message = LogMessage(message, arguments),
        )
    }

    /**
     * Logs a message with a [LogLevel.INFO] level.
     *
     * @param message The message.
     * @param arguments The arguments in a vararg.
     */
    fun info(
        message: String,
        vararg arguments: Pair<String, Any?>,
    ) {
        info(
            message = LogMessage.from(
                message = message,
                arguments = arguments,
            ),
        )
    }

    /**
     * Logs a message with a [LogLevel.INFO] level.
     *
     * @param message The message's details.
     */
    fun info(message: LogMessage) {
        log(
            source = source,
            level = LogLevel.INFO,
            message = message,
        )
    }

    /**
     * Logs a message with a [LogLevel.INFO] level.
     *
     * @param message The message.
     * @param error An error to accompany the message.
     */
    fun info(
        message: String,
        error: Throwable,
    ) {
        info(
            message = LogMessage(message),
            error = error,
        )
    }

    /**
     * Logs a message with a [LogLevel.INFO] level.
     *
     * For a boilerplate-free implementation, check out the overloaded function with vararg arguments.
     *
     * @param message The message.
     * @param arguments The arguments in a map.
     * @param error An error to accompany the message.
     *
     * @see info(message: String, vararg arguments: Pair<String, Any?>, error: Throwable)
     */
    fun info(
        message: String,
        arguments: Map<String, Any?>,
        error: Throwable,
    ) {
        info(
            message = LogMessage(
                message = message,
                args = arguments,
            ),
            error = error,
        )
    }

    /**
     * Logs a message with a [LogLevel.INFO] level.
     *
     * @param message The message.
     * @param arguments The arguments in a vararg.
     * @param error An error to accompany the message.
     */
    fun info(
        message: String,
        vararg arguments: Pair<String, Any?>,
        error: Throwable,
    ) {
        info(
            message = LogMessage.from(
                message = message,
                arguments = arguments,
            ),
            error = error,
        )
    }

    /**
     * Logs a message with a [LogLevel.INFO] level.
     *
     * @param message The message's details.
     * @param error An error to accompany the message.
     */
    fun info(
        message: LogMessage,
        error: Throwable,
    ) {
        log(
            source = source,
            level = LogLevel.INFO,
            message = message,
            error = error,
        )
    }

    /**
     * Logs a message with a [LogLevel.INFO] level using a custom [log tag][source].
     *
     * @param source A source tag to associate with this log message.
     * @param message The message.
     */
    fun info(
        source: String,
        message: String,
    ) {
        info(
            source = source,
            message = LogMessage(message),
        )
    }

    /**
     * Logs a message with a [LogLevel.INFO] level using a custom [log tag][source].
     *
     * @param source A source tag to associate with this log message.
     * @param message The message's details.
     */
    fun info(
        source: String,
        message: LogMessage,
    ) {
        log(
            source = source,
            level = LogLevel.INFO,
            message = message,
        )
    }

    /**
     * Logs a message with a [LogLevel.INFO] level using a custom [log tag][source].
     *
     * @param source A source tag to associate with this log message.
     * @param message The message's details.
     * @param error An error to accompany the message.
     */
    fun info(
        source: String,
        message: LogMessage,
        error: Throwable,
    ) {
        log(
            source = source,
            level = LogLevel.INFO,
            message = message,
            error = error,
        )
    }

    /**
     * Logs a message with a [LogLevel.WARNING] level.
     *
     * @param message The message.
     */
    fun warning(message: String) {
        warning(
            message = LogMessage(message),
        )
    }

    /**
     * Logs a message with a [LogLevel.WARNING] level.
     *
     * For a boilerplate-free implementation, check out the overloaded function with vararg arguments.
     *
     * @param message The message.
     * @param arguments The arguments in a map.
     *
     * @see warning(message: String, vararg arguments: Pair<String, Any?>)
     */
    fun warning(
        message: String,
        arguments: Map<String, Any?>,
    ) {
        warning(
            message = LogMessage(
                message = message,
                args = arguments,
            ),
        )
    }

    /**
     * Logs a message with a [LogLevel.WARNING] level.
     *
     * @param message The message.
     * @param arguments The arguments in a vararg.
     */
    fun warning(
        message: String,
        vararg arguments: Pair<String, Any?>,
    ) {
        warning(
            message = LogMessage.from(
                message = message,
                arguments = arguments,
            ),
        )
    }

    /**
     * Logs a message with a [LogLevel.WARNING] level.
     *
     * @param message The message's details.
     */
    fun warning(message: LogMessage) {
        log(
            source = source,
            level = LogLevel.WARNING,
            message = message,
        )
    }

    /**
     * Logs a message with a [LogLevel.WARNING] level.
     *
     * @param message The message.
     * @param error An error to accompany the message.
     */
    fun warning(
        message: String,
        error: Throwable,
    ) {
        warning(
            message = LogMessage(message),
            error = error,
        )
    }

    /**
     * Logs a message with a [LogLevel.WARNING] level.
     *
     * For a boilerplate-free implementation, check out the overloaded function with vararg arguments.
     *
     * @param message The message.
     * @param arguments The arguments in a map.
     * @param error An error to accompany the message.
     *
     * @see warning(message: String, vararg arguments: Pair<String, Any?>, error: Throwable)
     */
    fun warning(
        message: String,
        arguments: Map<String, Any?>,
        error: Throwable,
    ) {
        warning(
            message = LogMessage(
                message = message,
                args = arguments,
            ),
            error = error,
        )
    }

    /**
     * Logs a message with a [LogLevel.WARNING] level.
     *
     * @param message The message.
     * @param arguments The arguments in a vararg.
     * @param error An error to accompany the message.
     */
    fun warning(
        message: String,
        vararg arguments: Pair<String, Any?>,
        error: Throwable,
    ) {
        warning(
            message = LogMessage.from(
                message = message,
                arguments = arguments,
            ),
            error = error,
        )
    }

    /**
     * Logs a message with a [LogLevel.WARNING] level.
     *
     * @param message The message's details.
     * @param error An error to accompany the message.
     */
    fun warning(
        message: LogMessage,
        error: Throwable,
    ) {
        log(
            source = source,
            level = LogLevel.WARNING,
            message = message,
            error = error,
        )
    }

    /**
     * Logs a message with a [LogLevel.WARNING] level using a custom [log tag][source].
     *
     * @param source A source tag to associate with this log message.
     * @param message The message.
     */
    fun warning(
        source: String,
        message: String,
    ) {
        warning(
            source = source,
            message = LogMessage(message),
        )
    }

    /**
     * Logs a message with a [LogLevel.WARNING] level using a custom [log tag][source].
     *
     * @param source A source tag to associate with this log message.
     * @param message The message's details.
     */
    fun warning(
        source: String,
        message: LogMessage,
    ) {
        log(
            source = source,
            level = LogLevel.WARNING,
            message = message,
        )
    }

    /**
     * Logs a message with a [LogLevel.WARNING] level using a custom [log tag][source].
     *
     * @param source A source tag to associate with this log message.
     * @param message The message's details.
     * @param error An error to accompany the message.
     */
    fun warning(
        source: String,
        message: LogMessage,
        error: Throwable,
    ) {
        log(
            source = source,
            level = LogLevel.WARNING,
            message = message,
            error = error,
        )
    }

    /**
     * Logs a message with a [LogLevel.ERROR] level.
     *
     * @param message The message.
     */
    fun error(message: String) {
        error(
            message = LogMessage(message),
        )
    }

    /**
     * Logs a message with a [LogLevel.ERROR] level.
     *
     * For a boilerplate-free implementation, check out the overloaded function with vararg arguments.
     *
     * @param message The message.
     * @param arguments The arguments in a map.
     *
     * @see error(message: String, vararg arguments: Pair<String, Any?>)
     */
    fun error(
        message: String,
        arguments: Map<String, Any?>,
    ) {
        error(
            message = LogMessage(
                message = message,
                args = arguments,
            ),
        )
    }

    /**
     * Logs a message with a [LogLevel.ERROR] level.
     *
     * @param message The message.
     * @param arguments The arguments in a vararg.
     */
    fun error(
        message: String,
        vararg arguments: Pair<String, Any?>,
    ) {
        error(
            message = LogMessage.from(
                message = message,
                arguments = arguments,
            ),
        )
    }

    /**
     * Logs a message with a [LogLevel.ERROR] level.
     *
     * @param message The message's details.
     */
    fun error(message: LogMessage) {
        log(
            source = source,
            level = LogLevel.ERROR,
            message = message,
        )
    }

    /**
     * Logs a message with a [LogLevel.ERROR] level.
     *
     * @param message The message.
     * @param error An error to accompany the message.
     */
    fun error(
        message: String,
        error: Throwable,
    ) {
        error(
            message = LogMessage(message),
            error = error,
        )
    }

    /**
     * Logs a message with a [LogLevel.ERROR] level.
     *
     * For a boilerplate-free implementation, check out the overloaded function with vararg arguments.
     *
     * @param message The message.
     * @param arguments The arguments in a map.
     * @param error An error to accompany the message.
     *
     * @see error(message: String, vararg arguments: Pair<String, Any?>, error: Throwable)
     */
    fun error(
        message: String,
        arguments: Map<String, Any?>,
        error: Throwable,
    ) {
        error(
            message = LogMessage(
                message = message,
                args = arguments,
            ),
            error = error,
        )
    }

    /**
     * Logs a message with a [LogLevel.ERROR] level.
     *
     * @param message The message.
     * @param arguments The arguments in a vararg.
     * @param error An error to accompany the message.
     */
    fun error(
        message: String,
        vararg arguments: Pair<String, Any?>,
        error: Throwable,
    ) {
        error(
            message = LogMessage.from(
                message = message,
                arguments = arguments,
            ),
            error = error,
        )
    }

    /**
     * Logs a message with a [LogLevel.ERROR] level.
     *
     * @param message The message's details.
     * @param error An error to accompany the message.
     */
    fun error(
        message: LogMessage,
        error: Throwable,
    ) {
        log(
            source = source,
            level = LogLevel.ERROR,
            message = message,
            error = error,
        )
    }

    /**
     * Logs a message with a [LogLevel.ERROR] level using a custom [log tag][source].
     *
     * @param source A source tag to associate with this log message.
     * @param message The message.
     */
    fun error(
        source: String,
        message: String,
    ) {
        error(
            source = source,
            message = LogMessage(message),
        )
    }

    /**
     * Logs a message with a [LogLevel.ERROR] level using a custom [log tag][source].
     *
     * @param source A source tag to associate with this log message.
     * @param message The message's details.
     */
    fun error(
        source: String,
        message: LogMessage,
    ) {
        log(
            source = source,
            level = LogLevel.ERROR,
            message = message,
        )
    }

    /**
     * Logs a message with a [LogLevel.ERROR] level using a custom [log tag][source].
     *
     * @param source A source tag to associate with this log message.
     * @param message The message's details.
     * @param error An error to accompany the message.
     */
    fun error(
        source: String,
        message: LogMessage,
        error: Throwable,
    ) {
        log(
            source = source,
            level = LogLevel.ERROR,
            message = message,
            error = error,
        )
    }

    /**
     * Logs a message with a [LogLevel.CRASH] level.
     *
     * @param message The message.
     */
    fun crash(message: String) {
        crash(
            message = LogMessage(message),
        )
    }

    /**
     * Logs a message with a [LogLevel.CRASH] level.
     *
     * For a boilerplate-free implementation, check out the overloaded function with vararg arguments.
     *
     * @param message The message.
     * @param arguments The arguments in a map.
     *
     * @see crash(message: String, vararg arguments: Pair<String, Any?>)
     */
    fun crash(
        message: String,
        arguments: Map<String, Any?>,
    ) {
        crash(
            message = LogMessage(
                message = message,
                args = arguments,
            ),
        )
    }

    /**
     * Logs a message with a [LogLevel.CRASH] level.
     *
     * @param message The message.
     * @param arguments The arguments in a vararg.
     */
    fun crash(
        message: String,
        vararg arguments: Pair<String, Any?>,
    ) {
        crash(
            message = LogMessage.from(
                message,
                arguments,
            ),
        )
    }

    /**
     * Logs a message with a [LogLevel.CRASH] level.
     *
     * @param message The message's details.
     */
    fun crash(message: LogMessage) {
        log(
            source = source,
            level = LogLevel.CRASH,
            message = message,
        )
    }

    /**
     * Logs a message with a [LogLevel.CRASH] level.
     *
     * @param message The message.
     * @param error An error to accompany the message.
     */
    fun crash(
        message: String,
        error: Throwable,
    ) {
        crash(
            message = LogMessage(message),
            error = error,
        )
    }

    /**
     * Logs a message with a [LogLevel.CRASH] level.
     *
     * For a boilerplate-free implementation, check out the overloaded function with vararg arguments.
     *
     * @param message The message.
     * @param arguments The arguments in a map.
     * @param error An error to accompany the message.
     *
     * @see crash(message: String, vararg arguments: Pair<String, Any?>, error: Throwable)
     */
    fun crash(
        message: String,
        arguments: Map<String, Any?>,
        error: Throwable,
    ) {
        crash(
            message = LogMessage(
                message = message,
                args = arguments,
            ),
            error = error,
        )
    }

    /**
     * Logs a message with a [LogLevel.CRASH] level.
     *
     * @param message The message.
     * @param arguments The arguments in a vararg.
     * @param error An error to accompany the message.
     */
    fun crash(
        message: String,
        vararg arguments: Pair<String, Any?>,
        error: Throwable,
    ) {
        crash(
            message = LogMessage.from(
                message = message,
                arguments = arguments,
            ),
            error = error,
        )
    }

    /**
     * Logs a message with a [LogLevel.CRASH] level.
     *
     * @param message The message's details.
     * @param error An error to accompany the message.
     */
    fun crash(
        message: LogMessage,
        error: Throwable,
    ) {
        log(
            source = source,
            level = LogLevel.CRASH,
            message = message,
            error = error,
        )
    }

    /**
     * Logs a message with a [LogLevel.CRASH] level using a custom [log tag][source].
     *
     * @param source A source tag to associate with this log message.
     * @param message The message.
     */
    fun crash(
        source: String,
        message: String,
    ) {
        crash(
            source = source,
            message = LogMessage(message),
        )
    }

    /**
     * Logs a message with a [LogLevel.CRASH] level using a custom [log tag][source].
     *
     * @param source A source tag to associate with this log message.
     * @param message The message's details.
     */
    fun crash(
        source: String,
        message: LogMessage,
    ) {
        log(
            source = source,
            level = LogLevel.CRASH,
            message = message,
        )
    }

    /**
     * Logs a message with a [LogLevel.CRASH] level using a custom [log tag][source].
     *
     * @param source A source tag to associate with this log message.
     * @param message The message's details.
     * @param error An error to accompany the message.
     */
    fun crash(
        source: String,
        message: LogMessage,
        error: Throwable,
    ) {
        log(
            source = source,
            level = LogLevel.CRASH,
            message = message,
            error = error,
        )
    }

    /**
     * Logs a default message for a lifecycle change in the app.
     *
     * @param lifecycle The lifecycle change to log.
     */
    fun log(lifecycle: LogLifecycle) {
        info(
            message = LogLifecycle.format(
                event = lifecycle,
                decoration = decoration,
            ),
        )
    }

    /**
     * Logs a default message for a lifecycle change in the app using a custom [log tag][source].
     *
     * @param source A source tag that identifies the message.
     * @param lifecycle The lifecycle change to log.
     */
    fun log(
        source: String,
        lifecycle: LogLifecycle,
    ) {
        info(
            source = source,
            message = LogLifecycle.format(
                event = lifecycle,
                decoration = decoration,
            ),
        )
    }

    /**
     * Logs a message with the specified [level][LogLevel].
     *
     * @param level The level at which to log the message.
     * @param message The message's details.
     */
    fun log(
        level: LogLevel,
        message: LogMessage,
    ) {
        logAsync {
            log(
                source = source,
                level = level,
                message = message,
            )
        }
    }

    /**
     * Logs a message with the specified [level][LogLevel].
     *
     * @param level The level at which to log the message.
     * @param message The message's details.
     * @param error An error to accompany the message.
     */
    fun log(
        level: LogLevel,
        message: LogMessage,
        error: Throwable,
    ) {
        logAsync {
            log(
                source = source,
                level = level,
                message = message,
                error = error,
            )
        }
    }

    /**
     * Logs a message with the specified [level][LogLevel] using a custom [log tag][source].
     *
     * @param source A source tag that identifies the message.
     * @param level The level at which to log the message.
     * @param message The message's details.
     */
    fun log(
        source: String,
        level: LogLevel,
        message: LogMessage,
    ) {
        logAsync {
            log(
                source = source,
                level = level,
                message = message,
            )
        }
    }

    /**
     * Logs a message with the specified [level][LogLevel] using a custom [log tag][source].
     *
     * @param source A source tag that identifies the message.
     * @param level The level at which to log the message.
     * @param message The message's details.
     * @param error An error to accompany the message.
     */
    fun log(
        source: String,
        level: LogLevel,
        message: LogMessage,
        error: Throwable,
    ) {
        logAsync {
            log(
                source = source,
                level = level,
                message = message,
                error = error,
            )
        }
    }

    private fun logAsync(log: LoggerFacility.() -> Unit) {
        coroutineScope.launch {
            LoggerFacility
                .enabledFacilities
                .forEach {
                    it.log()
                }
        }
    }
}
