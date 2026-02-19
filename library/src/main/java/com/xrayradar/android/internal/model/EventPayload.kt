package com.xrayradar.android.internal.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class EventPayload(
    @SerialName("event_id")
    val eventId: String,
    val timestamp: String,
    val level: String,
    val message: String,
    val platform: String,
    val sdk: SdkInfo,
    val contexts: JsonObject,
    val breadcrumbs: List<BreadcrumbPayload> = emptyList(),
    val exception: ExceptionPayload? = null,
    val fingerprint: List<String>? = null,
)

@Serializable
internal data class SdkInfo(
    val name: String,
    val version: String,
)

@Serializable
internal data class BreadcrumbPayload(
    val timestamp: String,
    val message: String,
    val type: String = "default",
    val level: String = "info",
    val category: String? = null,
    val data: JsonObject? = null,
)

@Serializable
internal data class ExceptionPayload(
    val values: List<ExceptionValuePayload>,
)

@Serializable
internal data class ExceptionValuePayload(
    val type: String,
    val value: String,
    val stacktrace: StacktracePayload? = null,
)

@Serializable
internal data class StacktracePayload(
    val frames: List<StackFramePayload>,
)

@Serializable
internal data class StackFramePayload(
    val filename: String,
    val function: String,
    val lineno: Int,
    @SerialName("abs_path")
    val absPath: String,
    @SerialName("in_app")
    val inApp: Boolean,
)
