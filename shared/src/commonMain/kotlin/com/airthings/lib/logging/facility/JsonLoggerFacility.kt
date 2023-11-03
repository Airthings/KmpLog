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
import com.airthings.lib.logging.INITIAL_BLOCK_SIZE
import com.airthings.lib.logging.LogArgument
import com.airthings.lib.logging.LogDate
import com.airthings.lib.logging.LogLevel
import com.airthings.lib.logging.LogMessage
import com.airthings.lib.logging.LoggerFacility
import com.airthings.lib.logging.dateStamp
import com.airthings.lib.logging.datetimeStamp
import com.airthings.lib.logging.platform.PlatformDirectoryListing
import com.airthings.lib.logging.platform.PlatformFileInputOutput
import com.airthings.lib.logging.platform.PlatformFileInputOutputImpl
import com.airthings.lib.logging.platform.PlatformFileInputOutputNotifier
import com.airthings.lib.logging.utc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

/**
 * A [LoggerFacility] that persists log messages in a JSON file.
 *
 * @param minimumLogLevel Only log messages and errors that are equal or greater than this level.
 * @param baseFolder The base folder where log files will be written, excluding the trailing `/`.
 * @param coroutineScope A coroutine scope to run blocking i/o on.
 * @param notifier An optional implementation of [PlatformFileInputOutputNotifier].
 */
@Suppress("unused")
class JsonLoggerFacility(
    private val minimumLogLevel: LogLevel,
    private val baseFolder: String,
    private val coroutineScope: CoroutineScope,
    private val notifier: PlatformFileInputOutputNotifier?,
) : LoggerFacility {
    /**
     * Returns a [JsonLoggerFacility] instance that handles only [WARNING][LogLevel.WARNING],
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
     * Returns a [JsonLoggerFacility] instance without a notifier.
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
     * Returns a [JsonLoggerFacility] instance using a default [CoroutineScope].
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
        coroutineScope = FileLoggerFacility.loggerCoroutineScope(),
        notifier = notifier,
    )

    /**
     * Returns a [JsonLoggerFacility] instance that handles only [WARNING][LogLevel.WARNING],
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
        coroutineScope = FileLoggerFacility.loggerCoroutineScope(),
        notifier = null,
    )

    private val io: PlatformFileInputOutput = PlatformFileInputOutputImpl()
    private val currentLogFile = AtomicReference<String?>(null)

    init {
        coroutineScope.launch {
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

    override fun toString(): String = "JsonLoggerFacility($io)"

    override fun isEnabled(): Boolean = true

    override fun log(
        source: String,
        level: LogLevel,
        message: LogMessage,
    ) {
        withLogLevel(level) { logFile, prefix ->
            val jsonOutput = jsonOutput(
                source = source,
                time = utc(),
                level = level,
                message = message.message,
                error = null,
                args = message.args,
            )
            io.write(
                path = logFile,
                position = -1L,
                contents = "$prefix$jsonOutput$ARRAY_CLOSE",
            )
        }
    }

    override fun log(
        source: String,
        level: LogLevel,
        error: Throwable,
    ) {
        withLogLevel(level) { logFile, prefix ->
            val jsonOutput = jsonOutput(
                source = source,
                time = utc(),
                level = level,
                message = null,
                error = error,
                args = null,
            )
            io.write(
                path = logFile,
                position = -1L,
                contents = "$prefix$jsonOutput$ARRAY_CLOSE",
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

    private fun withLogLevel(
        level: LogLevel,
        action: suspend (jsonOutput: String, prefix: String) -> Unit,
    ) {
        if (level.value < minimumLogLevel.value) {
            return
        }

        coroutineScope.launch {
            val logFile = "$baseFolder${io.pathSeparator}${dateStamp(null)}.json"
            val currentLogFileLocked = currentLogFile.value

            // New JSON log files always contain an empty array ("[]") which is 2 bytes long.
            val isEmpty = io.size(logFile) > 2L

            if (currentLogFileLocked != logFile) {
                if (currentLogFileLocked != null) {
                    notifier?.onLogFileClosed(currentLogFileLocked)
                }
                io.ensure(logFile)

                // New JSON log files start their life as an empty array ("[]").
                io.append(logFile, "$ARRAY_OPEN$ARRAY_CLOSE")

                currentLogFile.set(logFile)
                notifier?.onLogFileOpened(logFile)
            }

            action(logFile, if (isEmpty) "" else ",")
        }
    }

    companion object {
        private const val LOG_TAG: String = "JsonLoggerFacility"
        private const val SOURCE_KEY: String = "source"
        private const val TIME_KEY: String = "time"
        private const val LEVEL_KEY: String = "level"
        private const val MESSAGE_KEY: String = "message"
        private const val ERROR_KEY: String = "error"
        private const val ARGS_KEY: String = "args"
        private const val COMMA: Char = ','
        private const val ARRAY_OPEN: Char = '['
        private const val ARRAY_CLOSE: Char = ']'
        private const val CURLY_OPEN: Char = '{'
        private const val CURLY_CLOSE: Char = '}'

        private fun String.jsonEscape(): String = replace("\\", "\\\\")
            .replace("/", "\\/")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("\t", "\\t")

        private fun String.jsonQuote(): String = "\"${jsonEscape()}\""

        private fun Pair<String, String>.jsonEntry(): String = "${first.jsonQuote()}:${second.jsonQuote()}"

        private fun List<LogArgument>.jsonEntry(): String = StringBuilder(INITIAL_BLOCK_SIZE)
            .apply {
                append(CURLY_OPEN)

                forEach { arg: LogArgument ->
                    val value = arg.value?.toString()
                    if (value != null) {
                        append((arg.label to value).jsonEntry())
                        append(COMMA)
                    }
                }

                // Remove the last comma if needed.
                if (length > 1) {
                    setLength(length - 1)
                }

                append(CURLY_CLOSE)
            }
            .toString()

        private fun jsonOutput(
            source: String,
            time: LocalDateTime,
            level: LogLevel,
            message: String?,
            error: Throwable?,
            args: List<LogArgument>?,
        ): String = StringBuilder(INITIAL_BLOCK_SIZE)
            .apply {
                append(CURLY_OPEN)

                append((SOURCE_KEY to source).jsonEntry())
                append(COMMA)
                append((TIME_KEY to datetimeStamp(time)).jsonEntry())
                append(COMMA)
                append((LEVEL_KEY to "$level").jsonEntry())

                if (message != null) {
                    append(COMMA)
                    append((MESSAGE_KEY to message).jsonEntry())
                }

                if (error != null) {
                    append(COMMA)
                    append((ERROR_KEY to error.stackTraceToString()).jsonEntry())
                }

                if (!args.isNullOrEmpty()) {
                    append(COMMA)
                    append(args.jsonEntry())
                }

                append(CURLY_CLOSE)
            }
            .toString()
    }
}
