package com.xrayradar.android

import com.xrayradar.android.internal.queue.nextBackoffMs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackoffTest {
    @Test
    fun `uses retry-after when present`() {
        assertEquals(30_000L, nextBackoffMs(attempts = 3, retryAfterSeconds = 30))
    }

    @Test
    fun `exponential backoff with cap`() {
        val first = nextBackoffMs(attempts = 0, retryAfterSeconds = null)
        val later = nextBackoffMs(attempts = 6, retryAfterSeconds = null)
        val capped = nextBackoffMs(attempts = 30, retryAfterSeconds = null)

        assertTrue(later > first)
        assertEquals(15 * 60 * 1_000L, capped)
    }

    @Test
    fun `attempt 0 gives 5s`() {
        assertEquals(5_000L, nextBackoffMs(attempts = 0, retryAfterSeconds = null))
    }

    @Test
    fun `attempt 1 gives 10s`() {
        assertEquals(10_000L, nextBackoffMs(attempts = 1, retryAfterSeconds = null))
    }

    @Test
    fun `retry-after zero uses exponential`() {
        assertEquals(5_000L, nextBackoffMs(attempts = 0, retryAfterSeconds = 0))
    }

    @Test
    fun `attempts above 10 still capped at 15 min`() {
        assertEquals(15 * 60 * 1_000L, nextBackoffMs(attempts = 10, retryAfterSeconds = null))
        assertEquals(15 * 60 * 1_000L, nextBackoffMs(attempts = 15, retryAfterSeconds = null))
    }
}
