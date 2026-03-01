package com.xrayradar.android.internal.transport

import com.xrayradar.android.internal.model.EventPayload
import com.xrayradar.android.internal.parseDsn
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

internal sealed class SendResult {
    data object Success : SendResult()
    data class Retryable(val retryAfterSeconds: Long?) : SendResult()
    data class Failure(val statusCode: Int?, val body: String?) : SendResult()
}

internal class HttpTransport(
    dsn: String,
    private val authToken: String,
    private val timeoutMs: Long = 10_000,
    private val userAgent: String = "xrayradar-android/0.1.0",
) {
    private val dsnParts = parseDsn(dsn)
    private val client = OkHttpClient.Builder()
        .callTimeout(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
        .build()
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun send(event: EventPayload): SendResult {
        val url = "${dsnParts.serverUrl}/api/${dsnParts.projectId}/store/"
        return try {
            val payload = json.encodeToString(event)
            TransportDebugLog.log("API POST $url (payload ${payload.length} bytes, event_id=${event.eventId})")
            val request = Request.Builder()
                .url(url)
                .post(payload.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", userAgent)
                .addHeader("X-Xrayradar-Token", authToken)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string()
                when {
                    response.isSuccessful -> {
                        TransportDebugLog.log("API response ${response.code} OK (event_id=${event.eventId})")
                        SendResult.Success
                    }
                    response.code == 429 -> {
                        val retryAfter = response.header("Retry-After")?.toLongOrNull()
                        TransportDebugLog.log("API response 429 rate limited, retryAfter=$retryAfter")
                        SendResult.Retryable(retryAfter)
                    }
                    response.code in 500..599 -> {
                        TransportDebugLog.log("API response ${response.code} server error, will retry")
                        SendResult.Retryable(null)
                    }
                    else -> {
                        TransportDebugLog.log("API response ${response.code} failure body=${bodyStr?.take(200)}")
                        SendResult.Failure(response.code, bodyStr)
                    }
                }
            }
        } catch (e: Exception) {
            TransportDebugLog.log("API request failed: ${e.javaClass.simpleName} ${e.message}")
            SendResult.Retryable(null)
        }
    }
}
