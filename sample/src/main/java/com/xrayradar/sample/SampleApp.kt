package com.xrayradar.sample

import android.app.Application
import com.xrayradar.android.ActivityBreadcrumbsIntegration
import com.xrayradar.android.LogBreadcrumbs
import com.xrayradar.android.XrayRadar
import com.xrayradar.android.XrayRadarOptions

class SampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val dsn = "https://xrayradar.com/<project_id>"
        val authToken = "replace-with-token"
        AppLogger.log("XrayRadar.init dsn=$dsn environment=development release=sample-1.0.0")

        XrayRadar.init(
            context = this,
            options = XrayRadarOptions(
                dsn = dsn,
                authToken = authToken,
                environment = "development",
                release = "sample-1.0.0",
                serverName = "android-emulator",
            ),
        )

        ActivityBreadcrumbsIntegration.install(this)
        LogBreadcrumbs.install()
        XrayRadar.setTransportDebugLogger { msg -> AppLogger.log(msg) }
        AppLogger.log("XrayRadar initialized. API interaction logs (POST, response, worker) will appear below when events are sent.")
    }
}
