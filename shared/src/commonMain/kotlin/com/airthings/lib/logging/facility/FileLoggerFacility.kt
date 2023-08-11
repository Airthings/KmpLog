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

import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import com.airthings.lib.logging.LogDate
import com.airthings.lib.logging.LogLevel
import com.airthings.lib.logging.LogMessage
import com.airthings.lib.logging.LoggerFacility
import com.airthings.lib.logging.dateStamp
import com.airthings.lib.logging.datetimeStampPrefix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

internal const val INITIAL_ARRAY_SIZE: Int = 50
internal const val LOG_FILE_EXTENSION: String = ".log"
internal const val LOG_FILE_SEPARATOR: Char = '-'

/**
 * A [LoggerFacility] that persists log messages to the file system.
 *
 * @param minimumLogLevel Only log messages and errors that are equal or greater than this level.
 * @param baseFolder The base folder where log files will be written, excluding the trailing `/`.
 * @param scope A coroutine scope to run blocking i/o on.
 * @param notifier An optional implementation of [PlatformFileInputOutputNotifier].
 */
@Suppress("unused")
class FileLoggerFacility constructor(
    private val minimumLogLevel: LogLevel,
    private val baseFolder: String,
    private val scope: CoroutineScope,
    private val notifier: PlatformFileInputOutputNotifier?
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
        notifier: PlatformFileInputOutputNotifier?
    ) : this(
        minimumLogLevel = LogLevel.WARNING,
        baseFolder = baseFolder,
        scope = scope,
        notifier = notifier
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
        scope = scope,
        notifier = null
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
        notifier: PlatformFileInputOutputNotifier?
    ) : this(
        minimumLogLevel = minimumLogLevel,
        baseFolder = baseFolder,
        scope = loggerCoroutineScope(),
        notifier = notifier
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
        baseFolder: String
    ) : this(
        minimumLogLevel = minimumLogLevel,
        baseFolder = baseFolder,
        scope = loggerCoroutineScope(),
        notifier = null
    )

    private val io: PlatformFileInputOutput = PlatformFileInputOutputImpl()
    private val currentLogFile = AtomicReference<String?>(null)

    init {
        scope.launch {
            if (!io.isDirectory(baseFolder) && !io.mkdirs(baseFolder)) {
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

    override fun log(source: String, level: LogLevel, message: LogMessage) {
        withLogLevel(level) { logFile ->
            io.append(
                logFile,
                "${datetimeStampPrefix()} ${PrinterLoggerFacility.format(level, message).trim()}$LF"
            )
        }
    }

    override fun log(source: String, level: LogLevel, error: Throwable) {
        withLogLevel(level) { logFile ->
            io.append(
                logFile,
                "${datetimeStampPrefix()} ${PrinterLoggerFacility.format(level, error).trim()}$LF"
            )
        }
    }

    /**
     * Scans the [baseFolder] and returns the list of log files residing in it.
     */
    suspend fun files(): Collection<String> = io.of(baseFolder)

    /**
     * Scans the [baseFolder] and returns the list of log files residing in it that were created after a certain date.
     *
     * @param date The date from which log files should be considered.
     */
    suspend fun files(date: LogDate): Collection<String> = io.of(baseFolder, date)

    private fun withLogLevel(level: LogLevel, action: suspend (String) -> Unit) {
        if (level.value < minimumLogLevel.value) {
            return
        }

        scope.launch {
            val logFile = "$baseFolder${io.pathSeparator}${dateStamp(null)}.log"
            val currentLogFileLocked = currentLogFile.value

            if (currentLogFileLocked != logFile) {
                if (currentLogFileLocked != null) {
                    io.close(currentLogFileLocked)
                    notifier?.onLogFileClosed(currentLogFileLocked)
                }
                currentLogFile.set(logFile)
                notifier?.onLogFileOpened(logFile)
            }

            action(logFile)
        }
    }

    companion object {
        private const val LF: Char = '\n'

        private inline fun loggerCoroutineScope(): CoroutineScope =
            CoroutineScope(Dispatchers.Main + SupervisorJob())

        /**
         * The log source tag for this class.
         */
        const val LOG_TAG: String = "FileLoggerFacility"
    }
}

/**
 * Defines a contract to retrieve the listing of files residing inside a directory.
 */
interface PlatformDirectoryListing {
    /**
     * Scans a [directory][path] and returns the list of log files residing in it.
     *
     * @param path The location of the directory to scan.
     */
    suspend fun of(path: String): Collection<String>

    /**
     * Scans a [directory][path] and returns the list of log files residing in it that were created after a certain date.
     *
     * @param path The location of the directory to scan.
     * @param date The date from which log files should be considered.
     */
    suspend fun of(path: String, date: LogDate): Collection<String>
}

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
     * Appends arbitrary bytes to a log file.
     *
     * @param path The location of the log file.
     * @param contents The contents to append to the file.
     */
    suspend fun append(path: String, contents: String)

    /**
     * Flushes any output waiting to be written to a log file and closes it.
     *
     * @param path The location of the log file.
     */
    suspend fun close(path: String)
}

/**
 * Defines a contract that notifies about opening and closing log files.
 */
interface PlatformFileInputOutputNotifier {
    /**
     * Invoked when a new log file has been created.
     *
     * @param path The location of the log file.
     */
    fun onLogFileOpened(path: String)

    /**
     * Invoked when a log file has been closed.
     *
     * @param path The location of the log file.
     */
    fun onLogFileClosed(path: String)
}

/**
 * Expect declaration for a [PlatformFileInputOutput].
 */
internal expect class PlatformFileInputOutputImpl constructor() : PlatformFileInputOutput
