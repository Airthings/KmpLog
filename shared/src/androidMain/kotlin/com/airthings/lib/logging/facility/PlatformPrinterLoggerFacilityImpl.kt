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

import android.util.Log
import com.airthings.lib.logging.PLATFORM_ANDROID
import com.airthings.lib.logging.LogLevel

/**
 * Implements logging in Android via the [android.util.Log] object.
 */
actual class PlatformPrinterLoggerFacilityImpl : PlatformPrinterLoggerFacility {
    override fun print(source: String, level: LogLevel, message: String) {
        when (level) {
            LogLevel.INFO -> Log.i(source, message)
            LogLevel.WARNING -> Log.w(source, message)
            LogLevel.ERROR -> Log.e(source, message)
            LogLevel.CRASH -> Log.wtf(source, message)
        }
    }

    override fun toString(): String = PLATFORM_ANDROID
}
