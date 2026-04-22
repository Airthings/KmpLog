package com.airthings.lib.logging.platform

import com.airthings.lib.logging.LogDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class DelegateFileInputOutputTest {

    @Test
    fun `pathSeparator is inherited from the underlying io`() {
        val io = FakeIo(pathSeparator = '|')
        val delegate = DelegateFileInputOutput(folder = "/logs", io = io)
        assertEquals('|', delegate.pathSeparator)
    }

    @Test
    fun `mkdirs delegates to the underlying io`() = runTest {
        val io = FakeIo()
        io.mkdirsResult["/logs/x"] = true
        val delegate = DelegateFileInputOutput(folder = "/logs", io = io)

        val result = delegate.mkdirs("/logs/x")

        assertTrue(result)
        assertTrue(io.calls.contains("mkdirs:/logs/x"))
    }

    @Test
    fun `size delegates to the underlying io`() = runTest {
        val io = FakeIo()
        io.sizeResult["/logs/a.log"] = 42L
        val delegate = DelegateFileInputOutput(folder = "/logs", io = io)

        val result = delegate.size("/logs/a.log")

        assertEquals(42L, result)
    }

    @Test
    fun `write delegates to the underlying io`() = runTest {
        val io = FakeIo()
        val delegate = DelegateFileInputOutput(folder = "/logs", io = io)

        delegate.write(path = "/logs/a.log", position = 5, contents = "data")

        assertTrue(io.calls.contains("write:/logs/a.log:5:data"))
    }

    @Test
    fun `append delegates to the underlying io`() = runTest {
        val io = FakeIo()
        val delegate = DelegateFileInputOutput(folder = "/logs", io = io)

        delegate.append(path = "/logs/a.log", contents = "data")

        assertTrue(io.calls.contains("append:/logs/a.log:data"))
    }

    @Test
    fun `ensure delegates to the underlying io`() = runTest {
        val io = FakeIo()
        val delegate = DelegateFileInputOutput(folder = "/logs", io = io)

        delegate.ensure("/logs/a.log")

        assertTrue(io.calls.contains("ensure:/logs/a.log"))
    }

    @Test
    fun `delete delegates to the underlying io`() = runTest {
        val io = FakeIo()
        val delegate = DelegateFileInputOutput(folder = "/logs", io = io)

        delegate.delete("/logs/a.log")

        assertTrue(io.calls.contains("delete:/logs/a.log"))
    }

    @Test
    fun `of without date delegates to the underlying io`() = runTest {
        val io = FakeIo()
        io.listingResult["/logs"] = listOf("/logs/a.log", "/logs/b.log")
        val delegate = DelegateFileInputOutput(folder = "/logs", io = io)

        assertEquals(listOf("/logs/a.log", "/logs/b.log"), delegate.of("/logs"))
    }

    @Test
    fun `of with date delegates to the underlying io`() = runTest {
        val io = FakeIo()
        val date = LogDate(2024, 3, 5)
        io.dateListingResult["/logs@$date"] = listOf("/logs/2024-03-05.log")
        val delegate = DelegateFileInputOutput(folder = "/logs", io = io)

        assertEquals(listOf("/logs/2024-03-05.log"), delegate.of("/logs", date))
    }

    @Test
    fun `onFolderMissing fires when the folder does not exist`() = runTest {
        val io = FakeIo()
        io.mkdirsResult["/logs"] = false // simulate folder missing
        var reported: String? = null
        val delegate = DelegateFileInputOutput(
            folder = "/logs",
            io = io,
            onFolderMissing = { reported = it },
        )

        delegate.ensure("/logs/a.log")

        assertEquals("/logs", reported)
    }

    @Test
    fun `onFolderMissing does not fire when the folder exists`() = runTest {
        val io = FakeIo()
        io.mkdirsResult["/logs"] = true // folder exists
        var reported: String? = null
        val delegate = DelegateFileInputOutput(
            folder = "/logs",
            io = io,
            onFolderMissing = { reported = it },
        )

        delegate.ensure("/logs/a.log")

        assertNull(reported)
    }

    @Test
    fun `reportMissingFolder throws IllegalStateException with the folder path`() {
        val error = assertFailsWith<IllegalStateException> {
            DelegateFileInputOutput.reportMissingFolder("/missing")
        }
        assertTrue(error.message.orEmpty().contains("/missing"))
    }
}

private class FakeIo(override val pathSeparator: Char = '/') : PlatformFileInputOutput {
    val calls = mutableListOf<String>()
    val mkdirsResult = mutableMapOf<String, Boolean>()
    val sizeResult = mutableMapOf<String, Long>()
    val listingResult = mutableMapOf<String, Collection<String>>()
    val dateListingResult = mutableMapOf<String, Collection<String>>()

    override suspend fun mkdirs(path: String): Boolean {
        calls += "mkdirs:$path"
        return mkdirsResult[path] ?: true
    }

    override suspend fun size(path: String): Long {
        calls += "size:$path"
        return sizeResult[path] ?: 0L
    }

    override suspend fun write(
        path: String,
        position: Long,
        contents: String,
    ) {
        calls += "write:$path:$position:$contents"
    }

    override suspend fun append(
        path: String,
        contents: String,
    ) {
        calls += "append:$path:$contents"
    }

    override suspend fun ensure(path: String) {
        calls += "ensure:$path"
    }

    override suspend fun delete(path: String) {
        calls += "delete:$path"
    }

    override suspend fun of(path: String): Collection<String> {
        calls += "of:$path"
        return listingResult[path] ?: emptyList()
    }

    override suspend fun of(
        path: String,
        date: LogDate,
    ): Collection<String> {
        calls += "of:$path:$date"
        return dateListingResult["$path@$date"] ?: emptyList()
    }
}
