package com.xrayradar.sample

import android.app.Activity
import android.os.Bundle
import com.xrayradar.android.XrayRadar

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        XrayRadar.addBreadcrumb(
            message = "MainActivity started",
            type = "navigation",
            category = "activity.lifecycle",
        )

        XrayRadar.captureMessage("Sample app launched", level = "info")
    }
}
