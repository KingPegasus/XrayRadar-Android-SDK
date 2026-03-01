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
        Log.i(TAG, message)
        logger?.invoke(full)
    }
}
