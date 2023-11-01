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

/**
 * Defines a contract to perform common input/output on files.
 *
 * The implementation of this interface is platform-specific and isn't in the shared module.
 */
internal interface PlatformFileInputOutput : PlatformDirectoryListing {
    /**
     * The character used to separate components of a path (`/`, or `\`, etc.)
     */
    val pathSeparator: Char

    /**
     * Returns true if the provided path points to a directory, false otherwise.
     *
     * @param path The location of the directory.
     */
    suspend fun isDirectory(path: String): Boolean

    /**
     * Creates the provided [directory path][path], including any intermediary ones, and returns true on success,
     * false otherwise.
     *
     * @param path The location of the directory.
     */
    suspend fun mkdirs(path: String): Boolean

    /**
     * Writes arbitrary bytes to a file starting at a specific position.
     *
     * For ease of use, [position] accepts negative values too. The difference between positive and negative
     * values is where from we start counting bytes from:
     *
     * - Positive [position] value: Number of bytes to count from starting at the beginning of the file.
     * - Negative [position] value: Number of bytes to count from starting at EOF (end-of-file).
     *
     * Note: Subsequent bytes after [position] are overwritten.
     *
     * @param path The location of the file.
     * @param position Number of bytes to position the file pointer at.
     * @param contents The contents to write to the file.
     */
    suspend fun write(path: String, position: Long, contents: String)

    /**
     * Appends arbitrary bytes to a file.
     *
     * @param path The location of the file.
     * @param contents The contents to append to the file.
     */
    suspend fun append(path: String, contents: String)

    /**
     * Ensures that a file exists at a specific location, creating it if necessary.
     *
     * @param path The location of the file.
     */
    suspend fun ensure(path: String)
}

/**
 * Platform-specific implementation of [PlatformFileInputOutput].
 */
internal expect class PlatformFileInputOutputImpl() : PlatformFileInputOutput

/**
 * Returns the absolute position within a file based on a relative position.
 */
internal fun Long.relativeToSize(size: Long): Long = (coerceAtMost(0L) + size).coerceIn(0L..size)
