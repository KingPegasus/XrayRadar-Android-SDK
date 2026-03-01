package com.xrayradar.android

import com.xrayradar.android.internal.model.EventPayload
import com.xrayradar.android.internal.model.SdkInfo
import com.xrayradar.android.internal.transport.HttpTransport
import com.xrayradar.android.internal.transport.SendResult
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpTransportTest {
    @Test
    fun `sends to ingest endpoint with token header`() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))
        server.start()

        try {
            val dsn = "${server.url("/")}".trimEnd('/') + "/1"
            val transport = HttpTransport(dsn = dsn, authToken = "test-token")
            val payload = EventPayload(
                eventId = "id1",
                timestamp = "2026-01-01T00:00:00Z",
                level = "error",
                message = "boom",
                platform = "android",
                sdk = SdkInfo(name = "xrayradar.android", version = "0.1.0"),
                contexts = buildJsonObject { put("environment", "test") },
            )
            val result = transport.send(payload)
            val req = server.takeRequest()

            assertTrue(result is SendResult.Success)
            assertEquals("/api/1/store/", req.path)
            assertEquals("test-token", req.getHeader("X-Xrayradar-Token"))
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `429 returns retryable with retry-after`() {
        val server = MockWebServer()
        server.enqueue(
            MockResponse()
                .setResponseCode(429)
                .addHeader("Retry-After", "12"),
        )
        server.start()
        try {
            val dsn = "${server.url("/")}".trimEnd('/') + "/1"
            val transport = HttpTransport(dsn = dsn, authToken = "test-token")
            val payload = EventPayload(
                eventId = "id1",
                timestamp = "2026-01-01T00:00:00Z",
                level = "error",
                message = "boom",
                platform = "android",
                sdk = SdkInfo(name = "xrayradar.android", version = "0.1.0"),
                contexts = buildJsonObject { put("environment", "test") },
            )
            val result = transport.send(payload)
            assertTrue(result is SendResult.Retryable)
            assertEquals(12L, (result as SendResult.Retryable).retryAfterSeconds)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `500 returns retryable`() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(500))
        server.start()
        try {
            val dsn = "${server.url("/")}".trimEnd('/') + "/1"
            val transport = HttpTransport(dsn = dsn, authToken = "test-token")
            val payload = EventPayload(
                eventId = "id1",
                timestamp = "2026-01-01T00:00:00Z",
                level = "error",
                message = "boom",
                platform = "android",
                sdk = SdkInfo(name = "xrayradar.android", version = "0.1.0"),
                contexts = buildJsonObject { put("environment", "test") },
            )
            val result = transport.send(payload)
            assertTrue(result is SendResult.Retryable)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `4xx returns failure`() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(400).setBody("Bad request"))
        server.start()
        try {
            val dsn = "${server.url("/")}".trimEnd('/') + "/1"
            val transport = HttpTransport(dsn = dsn, authToken = "test-token")
            val payload = EventPayload(
                eventId = "id1",
                timestamp = "2026-01-01T00:00:00Z",
                level = "error",
                message = "boom",
                platform = "android",
                sdk = SdkInfo(name = "xrayradar.android", version = "0.1.0"),
                contexts = buildJsonObject { put("environment", "test") },
            )
            val result = transport.send(payload)
            assertTrue(result is SendResult.Failure)
            assertEquals(400, (result as SendResult.Failure).statusCode)
            assertEquals("Bad request", result.body)
        } finally {
            server.shutdown()
        }
    }
}
