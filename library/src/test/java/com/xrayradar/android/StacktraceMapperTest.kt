package com.xrayradar.android

import com.xrayradar.android.internal.mapStacktrace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StacktraceMapperTest {
    @Test
    fun `maps frames and marks in_app`() {
        val throwable = IllegalStateException("boom")
        val frames = mapStacktrace(throwable, "com.xrayradar")
        assertTrue(frames.isNotEmpty())
        assertTrue(frames.any { it.inApp })
    }

    @Test
    fun `marks frames from other package as not in_app`() {
        val throwable = RuntimeException("x")
        val frames = mapStacktrace(throwable, "com.other.app")
        // Our test code runs under com.xrayradar or org.junit etc; frames from java.* etc are not in-app
        assertTrue(frames.any { !it.inApp } || frames.isEmpty())
    }

    @Test
    fun `frame has filename function lineno and absPath`() {
        val throwable = IllegalStateException("test")
        val frames = mapStacktrace(throwable, "com.xrayradar")
        assertTrue(frames.isNotEmpty())
        val first = frames.first()
        assertTrue(first.filename.isNotEmpty())
        assertTrue(first.function.contains("."))
        assertTrue(first.absPath.contains(first.filename))
        assertTrue(first.lineno >= 0)
    }

    @Test
    fun `empty stack trace returns empty list`() {
        val throwable = object : Throwable("no stack") {
            override fun getStackTrace(): Array<StackTraceElement> = emptyArray()
        }
        val frames = mapStacktrace(throwable, "com.xrayradar")
        assertTrue(frames.isEmpty())
    }
}
