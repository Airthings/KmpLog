package com.airthings.lib.logging.platform

import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class PlatformFileInputOutputImplTest {

    private lateinit var tempDir: File
    private val io = PlatformFileInputOutputImpl()

    @BeforeTest
    fun setUp() {
        tempDir = Files.createTempDirectory("kmplog-platform-io-").toFile()
    }

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `write at position -1 overwrites the trailing byte`() = runTest {
        // FileOutputStream(append=true) silently ignores channel.position(...) — the previous
        // implementation always wrote at EOF, so a write at position=-1 (intended to overwrite
        // the trailing "]" of a JSON array) was concatenated instead. RandomAccessFile honors
        // seek(), which is what JsonLoggerFacility relies on.
        val file = File(tempDir, "trail.json").apply { writeText("[{\"a\":1}]") }

        io.write(path = file.absolutePath, position = -1L, contents = ",{\"b\":2}]")

        assertEquals("[{\"a\":1},{\"b\":2}]", file.readText())
    }

    @Test
    fun `write at position 0 overwrites in place and extends a shorter file`() = runTest {
        // Used by JsonLoggerFacility to reseed a corrupted/short file (size 0 or 1) back to
        // a clean "[]". A 1-byte file is extended to 2 bytes by writing 2 bytes from position 0.
        val file = File(tempDir, "stub.json").apply { writeText("[") }

        io.write(path = file.absolutePath, position = 0L, contents = "[]")

        assertEquals("[]", file.readText())
    }
}
