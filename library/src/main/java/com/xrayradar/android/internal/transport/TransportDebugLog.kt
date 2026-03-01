package com.xrayradar.android.internal.transport

import android.util.Log

/**
 * Optional debug logger for API (transport) interactions.
 * Set via [com.xrayradar.android.XrayRadar.setTransportDebugLogger].
 */
internal object TransportDebugLog {
    private const val TAG = "XrayRadarTransport"

    var logger: ((String) -> Unit)? = null

    fun log(message: String) {
        val full = "[API] $message"
        try {
            Log.i(TAG, message)
        } catch (_: Throwable) {
            // Log may throw on JVM unit tests (no Android runtime)
        }
        try {
            logger?.invoke(full)
        } catch (_: Throwable) {
            // Avoid breaking transport if callback throws
        }
    }
}
