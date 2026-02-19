package com.xrayradar.android.internal

import com.xrayradar.android.internal.model.BreadcrumbPayload
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal class Scope(
    private var maxBreadcrumbs: Int,
) {
    private val lock = ReentrantLock()
    private val breadcrumbs = ArrayDeque<BreadcrumbPayload>()
    private var user: JsonObject? = null
    private val tags: MutableMap<String, String> = mutableMapOf()
    private val extra: MutableMap<String, Any?> = mutableMapOf()
    private val customContexts: MutableMap<String, JsonObject> = mutableMapOf()
    var environment: String = "development"
    var release: String = ""
    var serverName: String = "android"

    fun setMaxBreadcrumbs(max: Int) = lock.withLock {
        maxBreadcrumbs = max.coerceAtLeast(1)
        while (breadcrumbs.size > maxBreadcrumbs) breadcrumbs.removeFirst()
    }

    fun addBreadcrumb(crumb: BreadcrumbPayload) = lock.withLock {
        breadcrumbs.addLast(crumb)
        while (breadcrumbs.size > maxBreadcrumbs) breadcrumbs.removeFirst()
    }

    fun clearBreadcrumbs() = lock.withLock {
        breadcrumbs.clear()
    }

    fun getBreadcrumbs(): List<BreadcrumbPayload> = lock.withLock {
        breadcrumbs.toList()
    }

    fun setUser(value: Map<String, Any?>?) = lock.withLock {
        user = value?.let { map ->
            JsonObject(map.mapValues { (_, v) -> v.toJsonElement() })
        }
    }

    fun setTag(key: String, value: String) = lock.withLock {
        tags[key] = value
    }

    fun setExtra(key: String, value: Any?) = lock.withLock {
        extra[key] = value
    }

    fun setContext(key: String, data: Map<String, Any?>) = lock.withLock {
        customContexts[key] = JsonObject(data.mapValues { (_, v) -> v.toJsonElement() })
    }

    fun buildContexts(
        runtime: JsonObject,
        os: JsonObject,
        device: JsonObject,
        app: JsonObject,
    ): JsonObject = lock.withLock {
        buildJsonObject {
            put("environment", environment)
            if (release.isNotBlank()) put("release", release)
            if (serverName.isNotBlank()) put("server_name", serverName)
            user?.let { put("user", it) }
            putJsonObject("tags") {
                tags.forEach { (k, v) -> put(k, v) }
            }
            putJsonObject("extra") {
                extra.forEach { (k, v) -> put(k, v.toJsonElement()) }
            }
            put("runtime", runtime)
            put("os", os)
            put("device", device)
            put("app", app)
            customContexts.forEach { (k, v) -> put(k, v) }
        }
    }
}