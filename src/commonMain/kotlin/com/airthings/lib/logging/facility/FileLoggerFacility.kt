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

package com.airthings.lib.logging.facility

import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import com.airthings.lib.logging.LF
import com.airthings.lib.logging.LogDate
import com.airthings.lib.logging.LogLevel
import com.airthings.lib.logging.LogMessage
import com.airthings.lib.logging.LoggerFacility
import com.airthings.lib.logging.dateStamp
import com.airthings.lib.logging.datetimeStampPrefix
import com.airthings.lib.logging.platform.PlatformDirectoryListing
import com.airthings.lib.logging.platform.PlatformFileInputOutput
import com.airthings.lib.logging.platform.PlatformFileInputOutputImpl
import com.airthings.lib.logging.platform.PlatformFileInputOutputNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * A [LoggerFacility] that persists log messages to the file system.
 *
 * @param minimumLogLevel Only log messages and errors that are equal or greater than this level.
 * @param baseFolder The base folder where log files will be written, excluding the trailing `/`.
 * @param coroutineScope A coroutine scope to run blocking i/o on.
 * @param notifier An optional implementation of [PlatformFileInputOutputNotifier].
 */
class FileLoggerFacility(
    private val minimumLogLevel: LogLevel,
    val baseFolder: String,
    private val coroutineScope: CoroutineScope,
    private val notifier: PlatformFileInputOutputNotifier?,
) : LoggerFacility {
    /**
     * Returns a [FileLoggerFacility] instance that handles only [WARNING][LogLevel.WARNING],
     * [ERROR][LogLevel.ERROR] and [CRASH][LogLevel.CRASH] logs.
     *
     * If you'd like to log [INFO][LogLevel.INFO] also, then use the main constructor.
     *
     * @param baseFolder The base folder where log files will be written, excluding the trailing `/`.
     * @param scope A coroutine scope to run blocking i/o on.
     * @param notifier An optional implementation of [PlatformFileInputOutputNotifier].
     */
    constructor(
        baseFolder: String,
        scope: CoroutineScope,
        notifier: PlatformFileInputOutputNotifier?,
    ) : this(
        minimumLogLevel = LogLevel.WARNING,
        baseFolder = baseFolder,
        coroutineScope = scope,
        notifier = notifier,
    )

    /**
     * Returns a [FileLoggerFacility] instance.
     *
     * @param minimumLogLevel Only log messages and errors that are equal or greater than this level.
     * @param baseFolder The base folder where log files will be written, excluding the trailing `/`.
     * @param scope A coroutine scope to run blocking i/o on.
     */
    constructor(
        minimumLogLevel: LogLevel,
        baseFolder: String,
        scope: CoroutineScope,
    ) : this(
        minimumLogLevel = minimumLogLevel,
        baseFolder = baseFolder,
        coroutineScope = scope,
        notifier = null,
    )

    /**
     * Returns a [FileLoggerFacility] instance.
     *
     * @param minimumLogLevel Only log messages and errors that are equal or greater than this level.
     * @param baseFolder The base folder where log files will be written, excluding the trailing `/`.
     * @param notifier An optional implementation of [PlatformFileInputOutputNotifier].
     */
    constructor(
        minimumLogLevel: LogLevel,
        baseFolder: String,
        notifier: PlatformFileInputOutputNotifier?,
    ) : this(
        minimumLogLevel = minimumLogLevel,
        baseFolder = baseFolder,
        coroutineScope = loggerCoroutineScope(),
        notifier = notifier,
    )

    /**
     * Returns a [FileLoggerFacility] instance that handles only [WARNING][LogLevel.WARNING],
     * [ERROR][LogLevel.ERROR] and [CRASH][LogLevel.CRASH] logs.
     *
     * If you'd like to log [INFO][LogLevel.INFO] also, then use the main constructor.
     *
     * @param baseFolder The base folder where log files will be written, excluding the trailing `/`.
     */
    constructor(
        minimumLogLevel: LogLevel,
        baseFolder: String,
    ) : this(
        minimumLogLevel = minimumLogLevel,
        baseFolder = baseFolder,
        coroutineScope = loggerCoroutineScope(),
        notifier = null,
    )

    private val io: PlatformFileInputOutput = PlatformFileInputOutputImpl()
    private val currentLogFile = AtomicReference<String?>(null)

    init {
        coroutineScope.launch {
            // Please note: The call to `io.mkdirs()` returns true if the directory exists, which may be
            // different from the platform's implementation.
            if (!io.mkdirs(baseFolder)) {
                throw IllegalArgumentException("Base log folder is invalid: $baseFolder")
            }
        }
    }

    /**
     * Returns the platform-dependent [PlatformDirectoryListing] instance.
     */
    val listing: PlatformDirectoryListing
        get() = io

    override fun toString(): String = "FileLoggerFacility($io)"

    override fun isEnabled(): Boolean = true

    override fun log(
        source: String,
        level: LogLevel,
        message: LogMessage,
    ) {
        withLogLevel(level) { logFile ->
            io.append(
                path = logFile,
                contents = "${datetimeStampPrefix()} ${PrinterLoggerFacility.format(level, message).trim()}$LF",
            )
        }
    }

    override fun log(
        source: String,
        level: LogLevel,
        error: Throwable,
    ) {
        withLogLevel(level) { logFile ->
            io.append(
                path = logFile,
                contents = "${datetimeStampPrefix()} ${PrinterLoggerFacility.format(level, error).trim()}$LF",
            )
        }
    }

    override fun log(
        source: String,
        level: LogLevel,
        message: LogMessage,
        error: Throwable,
    ) {
        log(
            source = source,
            level = level,
            message = message,
        )
        log(
            source = source,
            level = level,
            error = error,
        )
    }

    /**
     * Scans the [baseFolder] and returns the list of log files residing in it.
     *
     * Note: The returned list contains absolute (canonical) paths to the files.
     */
    suspend fun files(): Collection<String> = io.of(baseFolder)

    /**
     * Scans the [baseFolder] and returns the list of log files residing in it that
     * were created after a certain date.
     *
     * Note: The returned list contains absolute (canonical) paths to the files.
     *
     * @param date The date from which log files should be considered.
     */
    suspend fun files(date: LogDate): Collection<String> = io.of(baseFolder, date)

    /**
     * Deletes a file residing in [baseFolder].
     *
     * @param name The file name to delete.
     */
    suspend fun delete(name: String) {
        io.delete("$baseFolder${io.pathSeparator}$name")
    }

    /**
     * Deletes a file at an absolute (canonical) location.
     *
     * @param path The file path to delete.
     */
    suspend fun deleteAbsolute(path: String) {
        io.delete(path)
    }

    private fun withLogLevel(
        level: LogLevel,
        action: suspend (String) -> Unit,
    ) {
        if (level.value < minimumLogLevel.value) {
            return
        }

        coroutineScope.launch {
            val logFile = "$baseFolder${io.pathSeparator}${dateStamp(null)}.log"
            val currentLogFileLocked = currentLogFile.value

            if (currentLogFileLocked != logFile) {
                if (currentLogFileLocked != null) {
                    notifier?.onLogFileClosed(currentLogFileLocked)
                }
                io.ensure(logFile)
                currentLogFile.set(logFile)
                notifier?.onLogFileOpened(logFile)
            }

            action(logFile)
        }
    }

    companion object {
        private const val LOG_TAG: String = "FileLoggerFacility"

        internal fun loggerCoroutineScope(): CoroutineScope =
            CoroutineScope(Dispatchers.Main + SupervisorJob())
    }
}
