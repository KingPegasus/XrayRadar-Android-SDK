package com.xrayradar.android

data class XrayRadarOptions(
    val dsn: String,
    val authToken: String,
    val debug: Boolean = false,
    val environment: String = "development",
    val release: String = "",
    val serverName: String = "android",
    val sampleRate: Double = 1.0,
    val maxBreadcrumbs: Int = 100,
    val maxPayloadBytes: Int = 100 * 1024,
)
