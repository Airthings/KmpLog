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

import com.airthings.lib.logging.message.NetworkLogMessage

/**
 * A [LogMessage] suitable for general use.
 *
 * When you extend this class, you must provide an implementation for [toString] which returns the final message
 * to be logged by the logger. The final message should contain all details of the [LogMessage] instance along
 * with its properties.
 *
 * For a default implementation, you can use the provided [LogMessage.format] function.
 *
 * @param message The message to log.
 * @param args List of arguments associated with the message.
 *
 * @see [NetworkLogMessage] shows how to concatenate the arguments from derivative implementations.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class LogMessage(
    val message: String,
    val args: List<LogArgument>,
) {
    /**
     * A [LogMessage] with no arguments.
     *
     * @param message The message to log.
     */
    constructor(message: String) : this(
        message = message,
        args = emptyList(),
    )

    /**
     * A [LogMessage] suitable for general use.
     *
     * For a boilerplate-free implementation, check out the overloaded constructor with vararg arguments.
     *
     * @param message The message to log.
     * @param args The arguments in a map.
     *
     * @see LogMessage(message: String, vararg arguments: Pair<String, Any?>)
     */
    constructor(
        message: String,
        args: Map<String, Any?>,
    ) : this(
        message = message,
        args = args.asLogArgs(),
    )

    /**
     * A [LogMessage] suitable for general use.
     *
     * @param message The message to log.
     * @param arguments The arguments in a vararg.
     */
    constructor(
        message: String,
        vararg arguments: Pair<String, Any?>,
    ) : this(
        message = message,
        args = arguments.asLogArgs(),
    )

    override fun toString(): String = format(message, args)

    companion object {
        /**
         * Returns a formatted log message containing a message and a list of [LogArgument]s.
         *
         * @param message The message to log.
         * @param args The list of [LogArgument]s to log.
         */
        fun format(message: String, args: List<LogArgument>): String = ArrayList<Any>(args.size + 1)
            .let {
                it.add(message.trim())
                args.forEach { arg ->
                    it.add(" $arg")
                }
                it.joinToString(separator = "")
            }
            .trim()

        @Suppress("SpreadOperator")
        internal fun from(message: String, arguments: Array<out Pair<String, Any?>>): LogMessage =
            LogMessage(message, *arguments)
    }
}
