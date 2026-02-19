package com.xrayradar.sample

import android.app.Application
import com.xrayradar.android.ActivityBreadcrumbsIntegration
import com.xrayradar.android.LogBreadcrumbs
import com.xrayradar.android.XrayRadar
import com.xrayradar.android.XrayRadarOptions

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        XrayRadar.init(
            context = this,
            options = XrayRadarOptions(
                dsn = "http://10.0.2.2:8001/1",
                authToken = "replace-with-token",
                environment = "development",
                release = "sample-1.0.0",
                serverName = "android-emulator",
            ),
        )

        ActivityBreadcrumbsIntegration.install(this)
        LogBreadcrumbs.install()
    }
}
