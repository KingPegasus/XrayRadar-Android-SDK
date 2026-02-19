package com.xrayradar.android

import android.util.Log

object LogBreadcrumbs {
    @Volatile
    private var installed = false

    fun install() {
        installed = true
    }

    fun uninstall() {
        installed = false
    }

    fun d(tag: String, message: String) = log("debug", tag, message)
    fun i(tag: String, message: String) = log("info", tag, message)
    fun w(tag: String, message: String) = log("warning", tag, message)
    fun e(tag: String, message: String, throwable: Throwable? = null) = log("error", tag, message, throwable)

    private fun log(level: String, tag: String, message: String, throwable: Throwable? = null) {
        when (level) {
            "debug" -> Log.d(tag, message, throwable)
            "info" -> Log.i(tag, message, throwable)
            "warning" -> Log.w(tag, message, throwable)
            else -> Log.e(tag, message, throwable)
        }
        if (!installed) return
        XrayRadar.addBreadcrumb(
            message = message,
            type = "console",
            level = level,
            category = "logcat.$tag",
            data = throwable?.let { mapOf("error" to (it.message ?: it::class.java.simpleName)) },
        )
    }
}
