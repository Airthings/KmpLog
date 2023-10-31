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

@file:Suppress("MagicNumber")

package com.airthings.lib.logging

import com.airthings.lib.logging.facility.LOG_FILE_EXTENSION
import com.airthings.lib.logging.facility.LOG_FILE_SEPARATOR
import kotlinx.datetime.LocalDateTime

/**
 * Holds a date corresponding with a log file, message, or otherwise.
 *
 * @param year The year part of the date.
 * @param month The month part of the date, within the range of 1..12.
 * @param day The day part of the date, within the range of 1..31.
 */
data class LogDate(
    val year: Int,
    val month: Int,
    val day: Int,
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

    override fun toString(): String = toString(LOG_FILE_SEPARATOR)

    override fun hashCode(): Int = toString(null).toIntOrNull() ?: toString().hashCode()
}

internal fun LogDate.toString(separator: Char?): String = StringBuilder(10)
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

internal fun String.asLogDate(separator: Char?): LogDate? {
    val expectedLength = if (separator == null) 8 else 10
    if (expectedLength != length) {
        return null
    }

    val intValue = (if (separator == null) toIntOrNull() else replace("$separator", "").toIntOrNull())
        ?: return null

    val dateValue = "$intValue"
    if (dateValue.length != 8) {
        return null
    }

    val year = dateValue.substring(0, 4).toIntOrNull() ?: return null
    val month = dateValue.substring(4, 6).toIntOrNull() ?: return null
    val day = dateValue.substring(6, 8).toIntOrNull() ?: return null

    return LogDate(year, month, day)
}

internal fun LogDate.after(another: LogDate): Boolean = year > another.year ||
    year == another.year && month > another.month ||
    month == another.month && day > another.day

/**
 * Returns true if this string denotes a log file created after [date], of if [date] is null, false otherwise.
 */
internal fun String.ifAfter(date: LogDate?): Boolean {
    val fileNameWithoutExtension = substringBeforeLast(LOG_FILE_EXTENSION)
    val logDate = fileNameWithoutExtension.asLogDate(LOG_FILE_SEPARATOR)

    return logDate != null && (date == null || logDate.after(date))
}
