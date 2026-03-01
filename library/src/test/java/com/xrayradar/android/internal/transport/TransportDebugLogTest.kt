package com.xrayradar.android.internal.transport

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TransportDebugLogTest {

    @Test
    fun `log invokes logger with API prefix when set`() {
        var received: String? = null
        TransportDebugLog.logger = { received = it }
        try {
            TransportDebugLog.log("test message")
            assertEquals("[API] test message", received)
        } finally {
            TransportDebugLog.logger = null
        }
    }

    @Test
    fun `log does not throw when logger is null`() {
        TransportDebugLog.logger = null
        TransportDebugLog.log("no listener")
    }

    @Test
    fun `clearing logger stops callbacks`() {
        var count = 0
        TransportDebugLog.logger = { count++ }
        TransportDebugLog.log("one")
        TransportDebugLog.logger = null
        TransportDebugLog.log("two")
        assertEquals(1, count)
    }
}
