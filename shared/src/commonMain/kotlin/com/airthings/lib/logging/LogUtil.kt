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

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Returns the current time in UTC.
 */
internal inline fun utc(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)

/**
 * Returns this [Int] padded with initial zeros up to the maximum [length].
 *
 * @param length Maximum length of resulted string.
 */
internal inline fun Int.padded(length: Int = 2): String = "$this".padStart(length, '0')

/**
 * Returns a string containing the specified [date][value] formatted as "YYYY-MM-DD".
 *
 * If the date is null, then the current time is used instead.
 */
internal fun dateStamp(value: LocalDateTime?): String {
    val normalizedValue = value ?: utc()
    val year = normalizedValue.year
    val month = normalizedValue.monthNumber.padded()
    val day = normalizedValue.dayOfMonth.padded()
    return "$year-$month-$day"
}

/**
 * Returns a string containing the specified [time][value] formatted as "HH:MM:SS".
 *
 * If the date is null, then the current time is used instead.
 */
internal fun timeStamp(value: LocalDateTime?): String {
    val normalizedValue = value ?: utc()
    val hour = normalizedValue.hour.padded()
    val minute = normalizedValue.minute.padded()
    val second = normalizedValue.second.padded()
    return "$hour:$minute:$second"
}

/**
 * Returns a string containing the specified [date and time][value] formatted as "YYYY-MM-DD HH:MM:SS".
 *
 * If the date is null, then the current time is used instead.
 */
internal fun datetimeStamp(value: LocalDateTime?): String {
    val normalizedValue = value ?: utc()
    return "${dateStamp(normalizedValue)} ${timeStamp(normalizedValue)}"
}

/**
 * Returns a prefix string containing a date and time stamp suitable to be pre-added to log messages.
 */
internal fun datetimeStampPrefix(): String = "[${datetimeStamp(null)}]:"
