package com.xrayradar.android

import com.xrayradar.android.internal.model.BreadcrumbPayload
import com.xrayradar.android.internal.model.EventPayload
import com.xrayradar.android.internal.model.ExceptionPayload
import com.xrayradar.android.internal.model.ExceptionValuePayload
import com.xrayradar.android.internal.model.SdkInfo
import com.xrayradar.android.internal.model.StackFramePayload
import com.xrayradar.android.internal.model.StacktracePayload
import com.xrayradar.android.internal.truncatePayload
import kotlinx.serialization.json.buildJsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TruncatePayloadTest {
    @Test
    fun `truncates message breadcrumbs and frames`() {
        val longMessage = "x".repeat(1200)
        val breadcrumbs = (1..150).map { i ->
            BreadcrumbPayload(timestamp = "t$i", message = "b$i")
        }
        val frames = (1..120).map { i ->
            StackFramePayload(
                filename = "f$i",
                function = "fn$i",
                lineno = i,
                absPath = "abs$i",
                inApp = true,
            )
        }

        val payload = EventPayload(
            eventId = "id",
            timestamp = "2026-01-01T00:00:00Z",
            level = "error",
            message = longMessage,
            platform = "android",
            sdk = SdkInfo("xrayradar.android", "0.1.0"),
            contexts = buildJsonObject {},
            breadcrumbs = breadcrumbs,
            exception = ExceptionPayload(listOf(ExceptionValuePayload("E", "V", StacktracePayload(frames)))),
        )

        val out = truncatePayload(payload, maxBreadcrumbs = 100, maxFrames = 50, maxMessageLength = 1000)
        assertEquals(100, out.breadcrumbs.size)
        assertEquals("b51", out.breadcrumbs.first().message)
        assertTrue(out.message.length <= 1000)
        assertEquals(50, out.exception?.values?.first()?.stacktrace?.frames?.size)
    }

    @Test
    fun `message exactly at max length is not truncated`() {
        val msg = "a".repeat(1000)
        val payload = EventPayload(
            eventId = "id",
            timestamp = "2026-01-01T00:00:00Z",
            level = "error",
            message = msg,
            platform = "android",
            sdk = SdkInfo("xrayradar.android", "0.1.0"),
            contexts = buildJsonObject {},
        )
        val out = truncatePayload(payload, maxMessageLength = 1000)
        assertEquals(1000, out.message.length)
        assertEquals(msg, out.message)
    }

    @Test
    fun `breadcrumbs exactly at max are kept`() {
        val breadcrumbs = (1..100).map { i -> BreadcrumbPayload("t$i", "m$i") }
        val payload = EventPayload(
            eventId = "id",
            timestamp = "2026-01-01T00:00:00Z",
            level = "error",
            message = "msg",
            platform = "android",
            sdk = SdkInfo("xrayradar.android", "0.1.0"),
            contexts = buildJsonObject {},
            breadcrumbs = breadcrumbs,
        )
        val out = truncatePayload(payload, maxBreadcrumbs = 100)
        assertEquals(100, out.breadcrumbs.size)
    }

    @Test
    fun `exception with null stacktrace is unchanged`() {
        val payload = EventPayload(
            eventId = "id",
            timestamp = "2026-01-01T00:00:00Z",
            level = "error",
            message = "msg",
            platform = "android",
            sdk = SdkInfo("xrayradar.android", "0.1.0"),
            contexts = buildJsonObject {},
            exception = ExceptionPayload(listOf(ExceptionValuePayload("E", "V", null))),
        )
        val out = truncatePayload(payload)
        assertTrue(out.exception!!.values[0].stacktrace == null)
    }
}
