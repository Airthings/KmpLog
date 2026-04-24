package com.airthings.lib.logging.platform

import com.airthings.lib.logging.LogDate
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * JVM-specific coverage for [PlatformFileInputOutputImpl]. Uses a real tmp directory via
 * `java.nio.file.Files`; the iOS counterpart lives in `iosTest/PlatformFileInputOutputImplTest.kt`.
 */
class PlatformFileInputOutputImplJvmTest {

    private lateinit var tempDir: java.io.File
    private val underTest = PlatformFileInputOutputImpl()

    @BeforeTest
    fun setUp() {
        tempDir = Files.createTempDirectory("kmplog-io-").toFile()
    }

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `pathSeparator matches the platform separator`() {
        assertEquals(java.io.File.separatorChar, underTest.pathSeparator)
    }

    @Test
    fun `size of a missing file is zero`() = runTest {
        assertEquals(0L, underTest.size("${tempDir.absolutePath}/does-not-exist.log"))
    }

    @Test
    fun `mkdirs creates the directory and returns true`() = runTest {
        val nested = "${tempDir.absolutePath}/a/b/c"
        assertTrue(underTest.mkdirs(nested))
        assertTrue(java.io.File(nested).isDirectory)
    }

    @Test
    fun `ensure creates an empty file in an existing folder`() = runTest {
        val path = "${tempDir.absolutePath}/empty.log"

        underTest.ensure(path)

        assertTrue(java.io.File(path).isFile)
        assertEquals(0L, underTest.size(path))
    }

    @Test
    fun `write creates the file content`() = runTest {
        val path = "${tempDir.absolutePath}/w.log"
        underTest.ensure(path)

        underTest.write(path, position = 0L, contents = "Hello")

        assertEquals("Hello", java.io.File(path).readText())
        assertEquals(5L, underTest.size(path))
    }

    @Test
    fun `append adds content to the file`() = runTest {
        val path = "${tempDir.absolutePath}/a.log"
        underTest.ensure(path)

        underTest.append(path, "Hello, ")
        underTest.append(path, "world")

        assertEquals("Hello, world", java.io.File(path).readText())
    }

    @Test
    fun `delete removes the file`() = runTest {
        val path = "${tempDir.absolutePath}/d.log"
        underTest.ensure(path)
        assertTrue(java.io.File(path).exists())

        underTest.delete(path)

        assertFalse(java.io.File(path).exists())
    }

    @Test
    fun `of returns the log files in the folder`() = runTest {
        val first = Files.createFile(tempDir.toPath().resolve("2024-03-05.log")).absolutePathString()
        val second = Files.createFile(tempDir.toPath().resolve("2024-03-06.log")).absolutePathString()

        val all = underTest.of(tempDir.absolutePath)

        assertEquals(2, all.size)
        assertContains(all, java.io.File(first).canonicalPath)
        assertContains(all, java.io.File(second).canonicalPath)
    }

    @Test
    fun `of with date returns only files newer than the cutoff`() = runTest {
        val older = Files.createFile(tempDir.toPath().resolve("2024-03-05.log")).absolutePathString()
        val newer = Files.createFile(tempDir.toPath().resolve("2024-03-07.log")).absolutePathString()

        val filtered = underTest.of(tempDir.absolutePath, LogDate(2024, 3, 6))

        assertEquals(1, filtered.size)
        assertContains(filtered, java.io.File(newer).canonicalPath)
        assertFalse(filtered.contains(java.io.File(older).canonicalPath))
    }

    @Test
    fun `of skips non-log files without a parseable date in the name`() = runTest {
        Files.createFile(tempDir.toPath().resolve("2024-03-05.log"))
        tempDir.toPath().resolve("readme.txt").writeText("not a log")

        val all = underTest.of(tempDir.absolutePath)

        assertEquals(1, all.size)
        assertTrue(all.first().endsWith("2024-03-05.log"))
    }

    @Test
    fun `toString identifies the platform`() {
        assertTrue(underTest.toString().isNotBlank())
    }
}
