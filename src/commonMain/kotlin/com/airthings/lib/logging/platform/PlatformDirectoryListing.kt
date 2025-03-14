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

package com.airthings.lib.logging.platform

import com.airthings.lib.logging.LogDate

/**
 * Defines a contract to retrieve the listing of files residing inside a directory.
 */
interface PlatformDirectoryListing {
    /**
     * Scans a [directory][path] and returns the list of log files residing in it.
     *
     * Note: The returned list contains absolute (canonical) paths to the files.
     *
     * @param path The location of the directory to scan.
     */
    suspend fun of(path: String): Collection<String>

    /**
     * Scans a [directory][path] and returns the list of log files residing in it that
     * were created after a certain date.
     *
     * Note: The returned list contains absolute (canonical) paths to the files.
     *
     * @param path The location of the directory to scan.
     * @param date The date from which log files should be considered.
     */
    suspend fun of(
        path: String,
        date: LogDate,
    ): Collection<String>
}
