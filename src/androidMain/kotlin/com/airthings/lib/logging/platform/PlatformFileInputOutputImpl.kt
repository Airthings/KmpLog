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

import com.airthings.lib.logging.INITIAL_ARRAY_SIZE
import com.airthings.lib.logging.LogDate
import com.airthings.lib.logging.PLATFORM_ANDROID
import com.airthings.lib.logging.ifAfter
import java.io.File
import java.io.FileOutputStream

internal actual class PlatformFileInputOutputImpl : PlatformFileInputOutput {
    private val writeLock = Any()

    override val pathSeparator: Char = File.separatorChar

    override suspend fun mkdirs(path: String): Boolean {
        val pathFile = File(path)

        pathFile.mkdirs()

        return pathFile.isDirectory
    }

    override suspend fun size(path: String): Long = File(path).length()

    override suspend fun write(path: String, position: Long, contents: String) {
        synchronized(writeLock) {
            FileOutputStream(File(path), true).use {
                val channel = it.channel
                val size = channel.size()
                val relativePosition = position.relativeToSize(size)

                channel.position(relativePosition)
                it.write(contents.toByteArray())
            }
        }
    }

    override suspend fun append(path: String, contents: String) {
        synchronized(writeLock) {
            FileOutputStream(File(path), true).use {
                it.write(contents.toByteArray())
            }
        }
    }

    override suspend fun ensure(path: String) {
        synchronized(writeLock) {
            File(path).createNewFile()
        }
    }

    override suspend fun of(path: String): Collection<String> = filesImpl(
        path = path,
        date = null,
    )

    override suspend fun of(path: String, date: LogDate): Collection<String> = filesImpl(
        path = path,
        date = date,
    )

    override fun toString(): String = PLATFORM_ANDROID

    private fun filesImpl(
        path: String,
        date: LogDate?,
    ): Collection<String> = ArrayList<String>(INITIAL_ARRAY_SIZE).apply {
        val iterator = File(path).walk().iterator()

        while (iterator.hasNext()) {
            val file = iterator.next()
            val canonicalPath = file.canonicalPath

            if (!file.isDirectory && canonicalPath.isNotBlank() && file.name.ifAfter(date)) {
                add(canonicalPath)
            }
        }
    }
}
