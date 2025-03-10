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
 * Holds minimal information about a log argument.
 *
 * You should provide an implementation for [toString] in classes implementing this interface such that it returns
 * a textual representation of both [label] and [value], at least.
 */
interface LogArgument {
    /**
     * The label of the argument.
     */
    val label: String

    /**
     * The value of the argument, may be null.
     */
    val value: Any?

    @Suppress("MemberVisibilityCanBePrivate")
    companion object {
        /**
         * Initial buffer size when constructing a textual representation of a [log argument][LogArgument].
         */
        private const val BUFFER_SIZE: Int = 512

        /**
         * Returns a textual representation of both [label] and [value].
         *
         * If [value] happen to be a [Map] or an [Iterable] or another type that includes other types, then this
         * function will recursively call itself in order to construct the member values' textual representation.
         *
         * If [value] is null, then the string "(null)" is used as a representation.
         *
         * @param label The label of the argument.
         * @param value The value of the argument, may be null.
         */
        fun format(
            label: String,
            value: Any?,
        ): String = "[$label=${formatValue(value)}]"

        /**
         * Returns a textual representation of [value].
         *
         * If [value] happen to be a [Map] or an [Iterable] or another type that includes other types, then this
         * function will recursively call itself in order to construct the member values' textual representation.
         *
         * If [value] is null, then the string "(null)" is used as a representation.
         *
         * @param value The value to format, may be null.
         */
        fun formatValue(value: Any?): String = when (value) {
            null -> "(null)"
            is CharSequence -> "\"$value\""
            is Map<*, *> -> formatMap(value)
            is Array<*> -> formatArray(value)
            is List<*> -> formatList(value)
            is Collection<*> -> formatCollection(value)
            is Iterable<*> -> formatIterable(value)
            else -> "$value"
        }

        /**
         * Returns a textual representation of [map].
         *
         * @param map The map to process.
         */
        fun formatMap(map: Map<*, *>): String = StringBuilder(BUFFER_SIZE)
            .apply {
                append("{")
                val iterator = map.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    append("${entry.key}: ${formatValue(entry.value)}")
                    if (iterator.hasNext()) {
                        append(", ")
                    }
                }
                append("}")
            }
            .toString()

        /**
         * Returns a textual representation of the provided [array].
         *
         * @param array The array to process.
         */
        fun formatArray(array: Array<*>): String = formatIterator(
            type = "Array",
            iterator = array.iterator(),
        )

        /**
         * Returns a textual representation of the provided [list].
         *
         * @param list The list to process.
         */
        fun formatList(list: List<*>): String = formatIterator(
            type = "List",
            iterator = list.iterator(),
        )

        /**
         * Returns a textual representation of the provided [collection].
         *
         * @param collection The Collection to process.
         */
        fun formatCollection(collection: Collection<*>): String = formatIterator(
            type = "Collection",
            iterator = collection.iterator(),
        )

        /**
         * Returns a textual representation of the provided [iterable].
         *
         * @param iterable The Iterable to process.
         */
        fun formatIterable(iterable: Iterable<*>): String = formatIterator(
            type = "Iterable",
            iterator = iterable.iterator(),
        )

        private fun formatIterator(
            type: String,
            iterator: Iterator<*>,
        ): String = StringBuilder(BUFFER_SIZE)
            .apply {
                append("$type(")
                while (iterator.hasNext()) {
                    val value = iterator.next()
                    append(formatValue(value))
                    if (iterator.hasNext()) {
                        append(", ")
                    }
                }
                append(")")
            }
            .toString()
    }
}

/**
 * A class implementing the [log argument][LogArgument] interface.
 *
 * Note that the [label] is in lowercase and dashes (-) are converted to underscore (_) characters.
 *
 * @param label The label of the argument.
 * @param value The value of the argument, may be null.
 */
class LogArg(label: String, override val value: Any?) : LogArgument {
    private val normalizedLabel: String = label.trim().lowercase().replace('-', '_')

    override val label: String = normalizedLabel

    override fun toString(): String = LogArgument.format(label, value)

    override fun equals(other: Any?): Boolean = other is LogArg && other.label == label && other.value == value

    override fun hashCode(): Int = toString().hashCode() + (value?.hashCode() ?: 0)
}

/**
 * Returns a List of [LogArg]s that corresponds with this [Map].
 */
internal fun Map<String, Any?>.asLogArgs(): List<LogArg> = map { LogArg(it.key, it.value) }.toList()

/**
 * Returns a List of [LogArg]s that corresponds with this [Array].
 */
internal fun Array<out Pair<String, Any?>>.asLogArgs(): List<LogArg> = map { LogArg(it.first, it.second) }
