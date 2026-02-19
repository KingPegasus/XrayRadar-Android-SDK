package com.xrayradar.android

import com.xrayradar.android.internal.mapStacktrace
import org.junit.Assert.assertTrue
import org.junit.Test

class StacktraceMapperTest {
    @Test
    fun `maps frames and marks in_app`() {
        val throwable = IllegalStateException("boom")
        val frames = mapStacktrace(throwable, "com.xrayradar")
        // At least one frame should be marked in-app for this test package.
        assertTrue(frames.any { it.inApp })
    }
}
