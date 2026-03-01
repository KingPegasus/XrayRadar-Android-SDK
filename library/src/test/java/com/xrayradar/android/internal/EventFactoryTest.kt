package com.xrayradar.android.internal

import com.xrayradar.android.internal.model.BreadcrumbPayload
import com.xrayradar.android.internal.model.EventPayload
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EventFactoryTest {

    @Test
    fun `messageEvent builds payload with message level and breadcrumbs`() {
        val scope = Scope(maxBreadcrumbs = 5)
        scope.addBreadcrumb(BreadcrumbPayload("t1", "crumb1"))
        scope.addBreadcrumb(BreadcrumbPayload("t2", "crumb2"))

        val payload = EventFactory.messageEvent(
            message = "Hello world",
            level = "info",
            scope = scope,
            packageName = "com.test.app",
            appVersionName = "1.0",
            appVersionCode = 1L,
        )

        assertEquals("Hello world", payload.message)
        assertEquals("info", payload.level)
        assertEquals("android", payload.platform)
        assertEquals(2, payload.breadcrumbs.size)
        assertEquals("crumb1", payload.breadcrumbs[0].message)
        assertEquals("crumb2", payload.breadcrumbs[1].message)
        assertNotNull(payload.eventId)
        assertTrue(payload.timestamp.isNotEmpty())
        assertEquals(listOf("Hello world"), payload.fingerprint)
        assertTrue(payload.contexts.containsKey("app"))
    }

    @Test
    fun `messageEvent normalizes unknown level to error`() {
        val scope = Scope(maxBreadcrumbs = 5)
        val payload = EventFactory.messageEvent(
            message = "x",
            level = "custom",
            scope = scope,
            packageName = "pkg",
            appVersionName = "1",
            appVersionCode = 1L,
        )
        assertEquals("error", payload.level)
    }

    @Test
    fun `exceptionEvent builds payload with exception and message override`() {
        val scope = Scope(maxBreadcrumbs = 2)
        scope.addBreadcrumb(BreadcrumbPayload("t1", "bread"))

        val throwable = IllegalStateException("original message")
        val payload = EventFactory.exceptionEvent(
            throwable = throwable,
            level = "error",
            messageOverride = "Custom message",
            scope = scope,
            packageName = "com.xrayradar.android",
            appVersionName = "2.0",
            appVersionCode = 2L,
        )

        assertEquals("Custom message", payload.message)
        assertEquals("error", payload.level)
        assertNotNull(payload.exception)
        assertEquals(1, payload.exception!!.values.size)
        assertEquals("IllegalStateException", payload.exception!!.values[0].type)
        assertEquals("original message", payload.exception!!.values[0].value)
        assertTrue(payload.exception!!.values[0].stacktrace!!.frames.isNotEmpty())
        assertEquals(1, payload.breadcrumbs.size)
        assertEquals("bread", payload.breadcrumbs[0].message)
    }

    @Test
    fun `exceptionEvent uses type and value when no message override`() {
        val scope = Scope(maxBreadcrumbs = 2)
        val throwable = RuntimeException("fail")
        val payload = EventFactory.exceptionEvent(
            throwable = throwable,
            level = "fatal",
            messageOverride = null,
            scope = scope,
            packageName = "pkg",
            appVersionName = "1",
            appVersionCode = 1L,
        )
        assertEquals("RuntimeException: fail", payload.message)
        assertEquals("fatal", payload.level)
    }

    @Test
    fun `exceptionEvent handles throwable with null message`() {
        val scope = Scope(maxBreadcrumbs = 2)
        val throwable = RuntimeException()
        val payload = EventFactory.exceptionEvent(
            throwable = throwable,
            level = "error",
            messageOverride = null,
            scope = scope,
            packageName = "pkg",
            appVersionName = "1",
            appVersionCode = 1L,
        )
        assertTrue(payload.message.contains("RuntimeException"))
        assertEquals("RuntimeException", payload.exception!!.values[0].type)
        assertEquals("RuntimeException", payload.exception!!.values[0].value)
    }
}
