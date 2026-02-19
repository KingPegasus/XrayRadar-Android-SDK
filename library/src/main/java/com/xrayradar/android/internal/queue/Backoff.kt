package com.xrayradar.android.internal.queue

internal fun nextBackoffMs(attempts: Int, retryAfterSeconds: Long?): Long {
    if (retryAfterSeconds != null && retryAfterSeconds > 0) {
        return retryAfterSeconds * 1_000
    }
    val exp = (1 shl attempts.coerceAtMost(10))
    return (exp * 5_000L).coerceAtMost(15 * 60 * 1_000L)
}
