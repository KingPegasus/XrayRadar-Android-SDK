package com.xrayradar.android

import com.xrayradar.android.internal.integrations.CrashHandler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashHandlerTest {
    @Test
    fun `captures and delegates to previous handler`() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        var delegated = false
        var captured: Throwable? = null
        val sentinel = Thread.UncaughtExceptionHandler { _, _ -> delegated = true }

        Thread.setDefaultUncaughtExceptionHandler(sentinel)
        try {
            val handler = CrashHandler { t -> captured = t }
            handler.uncaughtException(Thread.currentThread(), IllegalStateException("boom"))
            assertNotNull(captured)
            assertTrue(delegated)
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(previous)
        }
    }

    @Test
    fun `restore previous handler`() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        val sentinel = Thread.UncaughtExceptionHandler { _, _ -> }
        Thread.setDefaultUncaughtExceptionHandler(sentinel)
        try {
            val handler = CrashHandler { }
            Thread.setDefaultUncaughtExceptionHandler(null)
            handler.restorePrevious()
            assertEquals(sentinel, Thread.getDefaultUncaughtExceptionHandler())
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(previous)
        }
    }
}
