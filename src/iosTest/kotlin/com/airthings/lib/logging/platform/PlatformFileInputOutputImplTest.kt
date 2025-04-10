/*
 * Copyright 2025 Airthings ASA. All rights reserved.
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

@file:OptIn(ExperimentalUuidApi::class)

package com.airthings.lib.logging.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSBundle
import platform.Foundation.NSURL

class PlatformFileInputOutputImplTest {
    @Test
    fun `missing file`() = runTest {
        val path = mockPath()

        val underTest = PlatformFileInputOutputImpl()

        assertEquals(expected = 0, actual = underTest.size(path))
    }

    @Test
    fun `empty file`() = runTest {
        val path = mockPath()

        val underTest = PlatformFileInputOutputImpl()

        underTest.ensure(path)

        assertTrue { underTest.mkdirs(path) }
        assertEquals(expected = 0, actual = underTest.size(path))
    }

    @Test
    fun `write a single line`() = runTest {
        val path = mockPath()

        val underTest = PlatformFileInputOutputImpl()

        underTest.ensure(path)

        assertTrue { underTest.mkdirs(path) }
        assertEquals(expected = 0, actual = underTest.size(path))

        underTest.write(path, position = 0, contents = "Hello, world")

        assertEquals(expected = 12, actual = underTest.size(path))
    }

    @Test
    fun `append a single line`() = runTest {
        val path = mockPath()

        val underTest = PlatformFileInputOutputImpl()

        underTest.ensure(path)

        assertTrue { underTest.mkdirs(path) }
        assertEquals(expected = 0, actual = underTest.size(path))

        underTest.append(path, contents = "Hello, world")

        assertEquals(expected = 12, actual = underTest.size(path))
    }

    @Test
    fun `delete a file`() = runTest {
        val path = mockPath()

        val underTest = PlatformFileInputOutputImpl()

        underTest.ensure(path)

        assertTrue { underTest.mkdirs(path) }
        assertEquals(expected = 0, actual = underTest.size(path))

        underTest.append(path, contents = "Hello, world")

        assertEquals(expected = 12, actual = underTest.size(path))

        underTest.delete(path)

        assertEquals(expected = 0, actual = underTest.size(path))
    }
}

private fun mockPath(): String = NSURL(string = NSBundle.mainBundle.bundlePath)
    .URLByAppendingPathComponent(Uuid.random().toString())!!
    .URLByAppendingPathExtension("txt")!!
    .path!!
