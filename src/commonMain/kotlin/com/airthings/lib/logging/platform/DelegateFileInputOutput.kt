/*
 * Copyright 2024 Airthings ASA. All rights reserved.
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
import com.airthings.lib.logging.LogFolderMissingHandler

internal class DelegateFileInputOutput(
    private val folder: String,
    private val io: PlatformFileInputOutput,
    private val onFolderMissing: LogFolderMissingHandler? = null,
) : PlatformFileInputOutput {
    override val pathSeparator: Char = io.pathSeparator

    override suspend fun mkdirs(path: String): Boolean = ensureFolder {
        mkdirs(path)
    }

    override suspend fun size(path: String): Long = ensureFolder {
        size(path)
    }

    override suspend fun write(
        path: String,
        position: Long,
        contents: String,
    ) = ensureFolder {
        write(
            path = path,
            position = position,
            contents = contents,
        )
    }

    override suspend fun append(
        path: String,
        contents: String,
    ) = ensureFolder {
        append(
            path = path,
            contents = contents,
        )
    }

    override suspend fun ensure(path: String) = ensureFolder {
        ensure(path)
    }

    override suspend fun delete(path: String) = ensureFolder {
        delete(path)
    }

    override suspend fun of(path: String): Collection<String> = ensureFolder {
        of(path)
    }

    override suspend fun of(
        path: String,
        date: LogDate,
    ): Collection<String> = ensureFolder {
        of(
            path = path,
            date = date,
        )
    }

    private suspend fun <T> ensureFolder(
        action: suspend PlatformFileInputOutput.() -> T,
    ): T = with(io) {
        val isDirectoryExists = mkdirs(folder)
        try {
            io.action()
        } finally {
            if (!isDirectoryExists) {
                onFolderMissing?.invoke(folder)
            }
        }
    }

    companion object {
        /**
         * Default handler when a folder is invalid.
         *
         * @param folder The invalid folder's path.
         */
        fun reportMissingFolder(folder: String) {
            throw IllegalStateException("Log folder is invalid: $folder")
        }
    }
}
