package com.xrayradar.android.internal

import java.net.URI

internal data class DsnParts(
    val serverUrl: String,
    val projectId: String,
)

internal fun parseDsn(dsn: String): DsnParts {
    val uri = try {
        URI(dsn)
    } catch (_: Exception) {
        throw IllegalArgumentException("Invalid DSN format. Expected: https://xrayradar.com/your_project_id")
    }

    if (uri.scheme.isNullOrBlank() || uri.host.isNullOrBlank()) {
        throw IllegalArgumentException("Invalid DSN format. Expected: https://xrayradar.com/your_project_id")
    }

    val pathParts = (uri.path ?: "")
        .trim('/')
        .split('/')
        .filter { it.isNotBlank() }
    val projectId = pathParts.lastOrNull()
        ?: throw IllegalArgumentException("Missing project ID in DSN. Expected: https://xrayradar.com/your_project_id")

    val port = if (uri.port > 0) ":${uri.port}" else ""
    val serverUrl = "${uri.scheme}://${uri.host}$port"
    return DsnParts(serverUrl = serverUrl, projectId = projectId)
}
