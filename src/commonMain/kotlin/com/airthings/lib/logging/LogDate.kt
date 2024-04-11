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

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.airthings.lib.logging

import kotlinx.datetime.LocalDateTime

/**
 * Holds a date corresponding with a log file, message, or otherwise.
 *
 * @param year The year part of the date.
 * @param month The month part of the date, within the range of 1..12.
 * @param day The day part of the date, within the range of 1..31.
 * @param separator The character used to separate the date parts, defaults to [SEPARATOR].
 */
data class LogDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val separator: Char = SEPARATOR,
) {
    /**
     * Returns a [LogDate] instance from a [LocalDateTime] component.
     *
     * @param date The local date time component.
     */
    @Suppress("unused")
    constructor(date: LocalDateTime) : this(date.year, date.monthNumber, date.dayOfMonth)

    init {
        require(month in 1..12) { "The value of `month` must be within the range 1..12" }
        require(day in 1..31) { "The value of `day` must be within the range 1..31" }
    }

    override fun equals(other: Any?): Boolean =
        other is LogDate && other.year == year && other.month == month && other.day == day

    override fun toString(): String = toString(separator)

    override fun hashCode(): Int = toString(null).toIntOrNull() ?: toString().hashCode()

    companion object {
        /**
         * The default character used to separate date parts, f.ex: `2023-08-23`.
         */
        const val SEPARATOR = '-'

        internal const val LOG_FILE_LENGTH_WITHOUT_SEPARATOR = 8
        internal const val LOG_FILE_LENGTH_WITH_SEPARATOR = 10
    }
}

/**
 * Returns the string representation of this instance, optionally separated by a character.
 *
 * @param separator Optional separator between the file name's components.
 */
fun LogDate.toString(separator: Char?): String = StringBuilder(LogDate.LOG_FILE_LENGTH_WITH_SEPARATOR)
    .apply {
        append(year)
        if (separator != null) {
            append(separator)
        }
        append(month.padded())
        if (separator != null) {
            append(separator)
        }
        append(day.padded())
    }
    .toString()

/**
 * Converts this compatible string to a [LogDate] on success, null otherwise.
 *
 * @param separator Optional separator between the file name's components.
 */
@Suppress("MagicNumber")
fun String.asLogDate(separator: Char?): LogDate? {
    val expectedLength = if (separator == null) {
        LogDate.LOG_FILE_LENGTH_WITHOUT_SEPARATOR
    } else {
        LogDate.LOG_FILE_LENGTH_WITH_SEPARATOR
    }

    val fileNameWithoutExtension = substringBeforeLast('.')
    if (fileNameWithoutExtension.length != expectedLength) {
        return null
    }

    val dateAsIntValue = if (separator == null) {
        fileNameWithoutExtension.toIntOrNull()
    } else {
        fileNameWithoutExtension.replace("$separator", "").toIntOrNull()
    }

    if (dateAsIntValue == null) {
        return null
    }

    val dateAsStringValue = dateAsIntValue.padded(expectedLength)

    val year = dateAsStringValue.substring(0, 4).toIntOrNull() ?: return null
    val month = dateAsStringValue.substring(4, 6).toIntOrNull() ?: return null
    val day = dateAsStringValue.substring(6, 8).toIntOrNull() ?: return null

    return LogDate(
        year = year,
        month = month,
        day = day,
    )
}

/**
 * Returns true if this [LogDate] is newer than [another], false otherwise.
 *
 * @param another The other [LogDate] instance to compare against.
 */
fun LogDate.after(another: LogDate): Boolean = year > another.year ||
    year == another.year && month > another.month ||
    month == another.month && day > another.day

/**
 * Returns true if this string denotes a log file created after [date], or if [date] is null,
 * false otherwise.
 */
fun String.ifAfter(date: LogDate?): Boolean {
    val logDate = asLogDate(date?.separator ?: LogDate.SEPARATOR)

    return logDate != null && (date == null || logDate.after(date))
}
