package com.xrayradar.android

import com.xrayradar.android.internal.model.EventPayload
import com.xrayradar.android.internal.model.SdkInfo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertTrue
import org.junit.Test

class PayloadSerializationTest {
    private val json = Json { encodeDefaults = true }

    @Test
    fun `serializes stable payload fields`() {
        val payload = EventPayload(
            eventId = "abc",
            timestamp = "2026-01-01T00:00:00Z",
            level = "error",
            message = "hello",
            platform = "android",
            sdk = SdkInfo("xrayradar.android", "0.1.0"),
            contexts = buildJsonObject { put("environment", "test") },
        )

        val encoded = json.encodeToString(payload)
        assertTrue(encoded.contains("\"event_id\":\"abc\""))
        assertTrue(encoded.contains("\"platform\":\"android\""))
        assertTrue(encoded.contains("\"environment\":\"test\""))
    }

    @Test
    fun `serializes payload with exception and fingerprint`() {
        val payload = EventPayload(
            eventId = "e1",
            timestamp = "2026-01-01T00:00:00Z",
            level = "error",
            message = "fail",
            platform = "android",
            sdk = SdkInfo("xrayradar.android", "0.1.0"),
            contexts = buildJsonObject { put("env", "test") },
            breadcrumbs = emptyList(),
            exception = com.xrayradar.android.internal.model.ExceptionPayload(
                listOf(
                    com.xrayradar.android.internal.model.ExceptionValuePayload(
                        "RuntimeException",
                        "msg",
                        com.xrayradar.android.internal.model.StacktracePayload(emptyList()),
                    ),
                ),
            ),
            fingerprint = listOf("key1", "key2"),
        )
        val encoded = json.encodeToString(payload)
        assertTrue(encoded.contains("\"exception\""))
        assertTrue(encoded.contains("\"fingerprint\""))
        assertTrue(encoded.contains("key1"))
    }
}
