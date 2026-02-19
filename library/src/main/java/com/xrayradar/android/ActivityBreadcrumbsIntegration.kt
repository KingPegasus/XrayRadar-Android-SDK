package com.xrayradar.android

import android.app.Activity
import android.app.Application
import android.os.Bundle

object ActivityBreadcrumbsIntegration {
    private var callbacks: Application.ActivityLifecycleCallbacks? = null

    fun install(application: Application) {
        if (callbacks != null) return
        val cb = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                XrayRadar.addBreadcrumb(
                    message = "Activity created: ${activity::class.java.simpleName}",
                    type = "navigation",
                    category = "activity.created",
                )
            }

            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityResumed(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        }
        callbacks = cb
        application.registerActivityLifecycleCallbacks(cb)
    }

    fun uninstall(application: Application) {
        callbacks?.let { application.unregisterActivityLifecycleCallbacks(it) }
        callbacks = null
    }
}
