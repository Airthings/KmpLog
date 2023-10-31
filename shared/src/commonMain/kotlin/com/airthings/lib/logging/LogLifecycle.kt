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
 * A simplification of the commonly-used lifecycle events that both Android and iOS share.
 */
@Suppress("unused")
enum class LogLifecycle(private val value: String) {
    /**
     * The lifecycle state for when the app is just created, mostly for the first time.
     */
    CREATED("created"),

    /**
     * The lifecycle state for when the app is paused and shelved into the background.
     *
     * This may happen because the user switched her attention to a different app, or the operating system had
     * decided to pause the app for its own reasons, f.ex: A call just came in, or the phone automatically
     * locked after a duration of inactivity.
     */
    PAUSED("paused"),

    /**
     * The lifecycle state for when the app is resumed from background and shown to the user.
     */
    RESUMED("resumed"),

    /**
     * The lifecycle state for when the app has ended its execution because the user exited the app willingly.
     */
    FINISHED("finished"),

    /**
     * The lifecycle state for when the app has ended its execution because the operating system terminated it.
     */
    DESTROYED("destroyed");

    override fun toString(): String = value

    companion object {
        /**
         * Returns the default formatting of a lifecycle event, prefixed and suffixed by the provided values.
         *
         * @param event The lifecycle event to format.
         * @param decoration An optional decoration for the final formatting.
         */
        @Suppress("MagicNumber")
        fun format(
            event: LogLifecycle,
            decoration: LogDecoration? = null
        ): String = StringBuilder(128)
            .apply {
                if (decoration != null && !decoration.prefix.isNullOrEmpty()) {
                    append(decoration.prefix)
                }
                append("$event")
                if (decoration != null && !decoration.suffix.isNullOrEmpty()) {
                    append(decoration.suffix)
                }
            }
            .toString()
            .let {
                if (decoration == null || decoration.uppercase) it.uppercase() else it
            }
    }
}
