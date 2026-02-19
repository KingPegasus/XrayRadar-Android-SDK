package com.xrayradar.android

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.xrayradar.android.internal.EventFactory
import com.xrayradar.android.internal.Scope
import com.xrayradar.android.internal.integrations.CrashHandler
import com.xrayradar.android.internal.model.BreadcrumbPayload
import com.xrayradar.android.internal.model.EventPayload
import com.xrayradar.android.internal.normalizeLevel
import com.xrayradar.android.internal.nowIsoUtc
import com.xrayradar.android.internal.queue.QueueManager
import com.xrayradar.android.internal.truncatePayload
import com.xrayradar.android.internal.toJsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.random.Random

object XrayRadar {
    private var appContext: Context? = null
    private var options: XrayRadarOptions? = null
    private var scope: Scope? = null
    private var queueManager: QueueManager? = null
    private var crashHandler: CrashHandler? = null
    private var enabled: Boolean = false

    @Synchronized
    fun init(context: Context, options: XrayRadarOptions) {
        this.appContext = context.applicationContext
        this.options = options
        this.scope = Scope(maxBreadcrumbs = options.maxBreadcrumbs).also {
            it.environment = options.environment
            it.release = options.release
            it.serverName = options.serverName
        }
        this.queueManager = QueueManager(context.applicationContext, options.dsn, options.authToken)
        this.crashHandler = CrashHandler { throwable ->
            val event = buildExceptionEvent(throwable, "fatal", null)
            queueManager?.enqueueBlocking(event)
            queueManager?.triggerFlush()
        }
        Thread.setDefaultUncaughtExceptionHandler(crashHandler)
        this.enabled = true
    }

    @Synchronized
    fun captureException(
        throwable: Throwable,
        level: String = "error",
        message: String? = null,
        extras: Map<String, Any?> = emptyMap(),
    ): String? {
        if (!canCapture()) return null
        if (!shouldSample()) return null
        extras.forEach { (k, v) -> setExtra(k, v) }
        val event = buildExceptionEvent(throwable, level, message)
        enqueue(event)
        return event.eventId
    }

    @Synchronized
    fun captureMessage(
        message: String,
        level: String = "error",
        extras: Map<String, Any?> = emptyMap(),
    ): String? {
        if (!canCapture()) return null
        if (!shouldSample()) return null
        extras.forEach { (k, v) -> setExtra(k, v) }
        val event = buildMessageEvent(message, level)
        enqueue(event)
        return event.eventId
    }

    fun addBreadcrumb(
        message: String,
        type: String = "default",
        level: String = "info",
        category: String? = null,
        data: Map<String, Any?>? = null,
    ) {
        val s = scope ?: return
        s.addBreadcrumb(
            BreadcrumbPayload(
                timestamp = nowIsoUtc(),
                message = message,
                type = type,
                level = normalizeLevel(level),
                category = category,
                data = data?.let { JsonObject(it.mapValues { (_, v) -> v.toJsonElement() }) },
            ),
        )
    }

    fun clearBreadcrumbs() {
        scope?.clearBreadcrumbs()
    }

    fun setUser(user: Map<String, Any?>?) {
        scope?.setUser(user)
    }

    fun setTag(key: String, value: String) {
        scope?.setTag(key, value)
    }

    fun setExtra(key: String, value: Any?) {
        scope?.setExtra(key, value)
    }

    fun setContext(key: String, data: Map<String, Any?>) {
        scope?.setContext(key, data)
    }

    fun flush() {
        queueManager?.triggerFlush()
    }

    @Synchronized
    fun close() {
        enabled = false
        crashHandler?.restorePrevious()
        appContext = null
        options = null
        scope = null
        queueManager = null
        crashHandler = null
    }

    private fun canCapture(): Boolean = enabled && options != null && scope != null && queueManager != null

    private fun shouldSample(): Boolean {
        val sampleRate = options?.sampleRate ?: return false
        if (sampleRate <= 0.0) return false
        if (sampleRate >= 1.0) return true
        return Random.nextDouble() < sampleRate
    }

    private fun enqueue(event: EventPayload) {
        val opts = options ?: return
        val compact = truncatePayload(
            event = event,
            maxBreadcrumbs = opts.maxBreadcrumbs,
        )
        queueManager?.enqueue(compact)
        queueManager?.triggerFlush()
    }

    private fun buildExceptionEvent(throwable: Throwable, level: String, message: String?): EventPayload {
        val s = requireNotNull(scope)
        val ctx = requireNotNull(appContext)
        val pkg = ctx.packageName
        val info = getPackageInfo(ctx, pkg)
        val versionName = info.versionName ?: "unknown"
        val versionCode = if (Build.VERSION.SDK_INT >= 28) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
        return EventFactory.exceptionEvent(
            throwable = throwable,
            level = level,
            messageOverride = message,
            scope = s,
            packageName = pkg,
            appVersionName = versionName,
            appVersionCode = versionCode,
        )
    }

    private fun buildMessageEvent(message: String, level: String): EventPayload {
        val s = requireNotNull(scope)
        val ctx = requireNotNull(appContext)
        val pkg = ctx.packageName
        val info = getPackageInfo(ctx, pkg)
        val versionName = info.versionName ?: "unknown"
        val versionCode = if (Build.VERSION.SDK_INT >= 28) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
        return EventFactory.messageEvent(
            message = message,
            level = level,
            scope = s,
            packageName = pkg,
            appVersionName = versionName,
            appVersionCode = versionCode,
        )
    }

    private fun getPackageInfo(context: Context, packageName: String) = if (Build.VERSION.SDK_INT >= 33) {
        context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(packageName, 0)
    }
}