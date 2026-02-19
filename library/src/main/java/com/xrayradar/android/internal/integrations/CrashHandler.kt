package com.xrayradar.android.internal.integrations

internal class CrashHandler(
    private val capture: (Throwable) -> Unit,
) : Thread.UncaughtExceptionHandler {
    private val previous: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            capture(throwable)
        } catch (_: Exception) {
            // Best effort only on crash path.
        } finally {
            previous?.uncaughtException(thread, throwable)
        }
    }

    fun restorePrevious() {
        Thread.setDefaultUncaughtExceptionHandler(previous)
    }
}
