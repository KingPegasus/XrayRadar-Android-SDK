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
}
