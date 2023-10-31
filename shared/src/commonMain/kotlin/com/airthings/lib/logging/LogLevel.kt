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

/**
 * The different [log levels][LogMessage] that can be captured.
 *
 * @param label The [String] representation of the [LogLevel].
 * @param value The [Int] representation of the [LogLevel].
 * @param emoticon An human-friendly emoticon to identify the [LogLevel] in standard output.
 */
@Suppress("MagicNumber")
enum class LogLevel(
    @Suppress("MemberVisibilityCanBePrivate") val label: String,
    val value: Int,
    val emoticon: String
) {
    /**
     * An informative event, used mainly to record information about the app's normal operation.
     */
    INFO("info", 0, "🔍️"),

    /**
     * An unforeseen and unplanned event that's obstructing the app, but isn't causing it to malfunction.
     */
    WARNING("warning", 10, "😱"),

    /**
     * An unforeseen and unplanned event that's obstructing the app and is causing it to malfunction, but not
     * crash.
     *
     * Usually the user is able to remedy the situation by simply repeating the operation that caused the error.
     */
    ERROR("error", 90, "🐞"),

    /**
     * An event that caused the app to crash unexpectedly.
     */
    CRASH("crash", 99, "💥");

    override fun toString(): String = label.uppercase()
}
