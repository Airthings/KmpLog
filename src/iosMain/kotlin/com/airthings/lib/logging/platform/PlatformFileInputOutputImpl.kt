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

@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.airthings.lib.logging.platform

import com.airthings.lib.logging.INITIAL_ARRAY_SIZE
import com.airthings.lib.logging.LogDate
import com.airthings.lib.logging.PLATFORM_IOS
import com.airthings.lib.logging.ifAfter
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.BooleanVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSFileHandle
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.closeFile
import platform.Foundation.create
import platform.Foundation.fileHandleForWritingAtPath
import platform.Foundation.seekToEndOfFile
import platform.Foundation.seekToFileOffset

@OptIn(ExperimentalForeignApi::class)
private typealias NSERROR_CPOINTER = CPointer<ObjCObjectVar<NSError?>>

@OptIn(ExperimentalForeignApi::class)
internal actual class PlatformFileInputOutputImpl : PlatformFileInputOutput {
    actual override val pathSeparator: Char = '/'

    actual override suspend fun size(path: String): Long = nsErrorWrapper(0L) {
        sizeImpl(path)
    }

    actual override suspend fun mkdirs(path: String): Boolean {
        nsErrorWrapper(false) {
            NSFileManager.defaultManager.createDirectoryAtPath(
                path = path,
                withIntermediateDirectories = true,
                attributes = null,
                error = this,
            )
        }

        return isDirectory(path)
    }

    actual override suspend fun write(
        path: String,
        position: Long,
        contents: String,
    ) {
        nsErrorWrapper(Unit) {
            val file = NSFileHandle.fileHandleForWritingAtPath(path)
                ?: throw IllegalArgumentException("Cannot open file for writing: $path")

            val size = sizeImpl(path)

            val relativePosition = position.relativeToSize(size).toULong()

            try {
                file.seekToFileOffset(relativePosition)
                file.writeData(data = contents.asNSData(), error = this)
            } finally {
                file.closeFile()
            }
        }
    }

    actual override suspend fun append(
        path: String,
        contents: String,
    ) {
        nsErrorWrapper(Unit) {
            val file = NSFileHandle.fileHandleForWritingAtPath(path)
                ?: throw IllegalArgumentException("Cannot open file for appending: $path")

            try {
                file.seekToEndOfFile()
                file.writeData(contents.asNSData(), error = this)
            } finally {
                file.closeFile()
            }
        }
    }

    actual override suspend fun ensure(path: String) {
        val exists = memScoped {
            val cValue = alloc<BooleanVar>()
            cValue.value = false
            NSFileManager.defaultManager.fileExistsAtPath(
                path = path,
                isDirectory = cValue.ptr,
            )
        }
        if (!exists) {
            nsErrorWrapper(Unit) {
                NSFileManager.defaultManager.createFileAtPath(path, contents = null, attributes = null)
            }
        }
    }

    actual override suspend fun delete(path: String) {
        nsErrorWrapper(Unit) {
            NSFileManager.defaultManager.removeItemAtPath(
                path = path,
                error = this,
            )
        }
    }

    actual override suspend fun of(path: String): Collection<String> = filesImpl(
        path = path,
        date = null,
    )

    actual override suspend fun of(
        path: String,
        date: LogDate,
    ): Collection<String> = filesImpl(
        path = path,
        date = date,
    )

    override fun toString(): String = PLATFORM_IOS

    private fun isDirectory(path: String): Boolean = memScoped {
        val cValue = alloc<BooleanVar>()
        cValue.value = true
        NSFileManager.defaultManager.fileExistsAtPath(
            path = path,
            isDirectory = cValue.ptr,
        )
    }

    private fun NSERROR_CPOINTER.sizeImpl(path: String): Long {
        val fileAttributes = NSFileManager.defaultManager.attributesOfItemAtPath(path, this)
        return fileAttributes?.getValue("NSFileSize")?.toString()?.toLong() ?: 0
    }

    private fun filesImpl(
        path: String,
        date: LogDate?,
    ): Collection<String> = ArrayList<String>(INITIAL_ARRAY_SIZE).apply {
        val fm = NSFileManager.defaultManager
        val enumerator = fm.enumeratorAtPath(path)

        if (enumerator != null) {
            enumerator.skipDescendents()

            do {
                val filename = enumerator.nextObject()

                if (filename is String && filename.isNotBlank() && filename.ifAfter(date)) {
                    try {
                        NSURL(string = path)
                            .URLByAppendingPathComponent(filename)
                            ?.absoluteString
                            ?.apply(::add)
                    } catch (ignored: Throwable) {
                        // This file caused some kind of error; we'll ignore it, and let
                        // the loop process the next entry.
                    }
                }
            } while (filename != null)
        }
    }
}

/**
 * Allows running a [block] that requires a [NSError] for storing any possible errors.
 *
 * If after running [block] there is an error, then [fallback] is returned.
 * Otherwise, the actual result of [block] is returned.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun <T> nsErrorWrapper(
    fallback: T,
    block: NSERROR_CPOINTER.() -> T,
): T {
    memScoped {
        val errorPointer: NSERROR_CPOINTER = alloc<ObjCObjectVar<NSError?>>().ptr
        val result: T = block(errorPointer)
        val error: NSError? = errorPointer.pointed.value

        return if (error == null) result else fallback
    }
}

fun String.asNSData(): NSData = encodeToByteArray().asNSData()

fun ByteArray.asNSData(): NSData = memScoped {
    NSData.create(
        bytes = allocArrayOf(this@asNSData),
        length = this@asNSData.size.toULong(),
    )
}
