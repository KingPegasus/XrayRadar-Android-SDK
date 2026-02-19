package com.xrayradar.android.internal

import android.os.Build
import com.xrayradar.android.internal.model.BreadcrumbPayload
import com.xrayradar.android.internal.model.EventPayload
import com.xrayradar.android.internal.model.ExceptionPayload
import com.xrayradar.android.internal.model.ExceptionValuePayload
import com.xrayradar.android.internal.model.SdkInfo
import com.xrayradar.android.internal.model.StacktracePayload
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

internal object EventFactory {
    private const val SDK_NAME = "xrayradar.android"
    private const val SDK_VERSION = "0.1.0"

    fun messageEvent(
        message: String,
        level: String,
        scope: Scope,
        packageName: String,
        appVersionName: String,
        appVersionCode: Long,
    ): EventPayload {
        val normalizedLevel = normalizeLevel(level)
        return EventPayload(
            eventId = UUID.randomUUID().toString(),
            timestamp = nowIsoUtc(),
            level = normalizedLevel,
            message = message,
            platform = "android",
            sdk = SdkInfo(SDK_NAME, SDK_VERSION),
            contexts = buildContexts(scope, packageName, appVersionName, appVersionCode),
            breadcrumbs = scope.getBreadcrumbs(),
            fingerprint = listOf(message),
        )
    }

    fun exceptionEvent(
        throwable: Throwable,
        level: String,
        messageOverride: String?,
        scope: Scope,
        packageName: String,
        appVersionName: String,
        appVersionCode: Long,
    ): EventPayload {
        val frames = mapStacktrace(throwable, packageName)

        val type = throwable::class.java.simpleName.ifBlank { "Exception" }
        val value = throwable.message ?: type
        val message = messageOverride ?: "$type: $value"

        val exception = ExceptionPayload(
            values = listOf(
                ExceptionValuePayload(
                    type = type,
                    value = value,
                    stacktrace = StacktracePayload(frames),
                ),
            ),
        )

        return EventPayload(
            eventId = UUID.randomUUID().toString(),
            timestamp = nowIsoUtc(),
            level = normalizeLevel(level),
            message = message,
            platform = "android",
            sdk = SdkInfo(SDK_NAME, SDK_VERSION),
            contexts = buildContexts(scope, packageName, appVersionName, appVersionCode),
            breadcrumbs = scope.getBreadcrumbs(),
            exception = exception,
            fingerprint = listOfNotNull(type, value, frames.firstOrNull()?.function),
        )
    }

    private fun buildContexts(
        scope: Scope,
        packageName: String,
        appVersionName: String,
        appVersionCode: Long,
    ) = scope.buildContexts(
        runtime = buildJsonObject {
            put("name", "android-runtime")
            put("version", Build.VERSION.RELEASE ?: "unknown")
        },
        os = buildJsonObject {
            put("name", "Android")
            put("version", Build.VERSION.RELEASE ?: "unknown")
            put("api_level", Build.VERSION.SDK_INT)
        },
        device = buildJsonObject {
            put("manufacturer", Build.MANUFACTURER ?: "unknown")
            put("model", Build.MODEL ?: "unknown")
            put("brand", Build.BRAND ?: "unknown")
        },
        app = buildJsonObject {
            put("package_name", packageName)
            put("version_name", appVersionName)
            put("version_code", appVersionCode)
        },
    )
}

internal fun normalizeLevel(level: String): String {
    return when (level.lowercase()) {
        "fatal", "error", "warning", "info", "debug" -> level.lowercase()
        else -> "error"
    }
}

internal fun truncatePayload(
    event: EventPayload,
    maxBreadcrumbs: Int = 100,
    maxFrames: Int = 50,
    maxMessageLength: Int = 1000,
): EventPayload {
    val msg = if (event.message.length > maxMessageLength) {
        "${event.message.take(maxMessageLength - 3)}..."
    } else {
        event.message
    }
    val breadcrumbs: List<BreadcrumbPayload> = if (event.breadcrumbs.size > maxBreadcrumbs) {
        event.breadcrumbs.takeLast(maxBreadcrumbs)
    } else {
        event.breadcrumbs
    }
    val exception = event.exception?.let { ex ->
        ex.copy(values = ex.values.map { value ->
            val frames = value.stacktrace?.frames
            if (frames != null && frames.size > maxFrames) {
                value.copy(stacktrace = StacktracePayload(frames.take(maxFrames)))
            } else {
                value
            }
        })
    }
    return event.copy(message = msg, breadcrumbs = breadcrumbs, exception = exception)
}
