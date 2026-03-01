package com.xrayradar.sample

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.xrayradar.android.NetworkBreadcrumbInterceptor
import com.xrayradar.android.XrayRadar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class MainActivity : Activity() {

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(NetworkBreadcrumbInterceptor())
        .build()

    private lateinit var logText: TextView
    private lateinit var logScroll: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logText = findViewById(R.id.log_text)
        logScroll = findViewById(R.id.log_scroll)

        AppLogger.listener = object : AppLogger.Listener {
            override fun onLogUpdated(fullText: String) {
                logText.text = fullText
                logScroll.post { logScroll.fullScroll(ScrollView.FOCUS_DOWN) }
            }
        }
        logText.text = AppLogger.getAllLines()
        logScroll.post { logScroll.fullScroll(ScrollView.FOCUS_DOWN) }

        findViewById<Button>(R.id.btn_clear_log).setOnClickListener {
            AppLogger.clear()
            Toast.makeText(this, "Log cleared", Toast.LENGTH_SHORT).show()
        }

        AppLogger.log("onCreate: adding breadcrumb and captureMessage for app launch")
        XrayRadar.addBreadcrumb(
            message = "MainActivity started",
            type = "navigation",
            category = "activity.lifecycle",
        )
        val launchEventId = XrayRadar.captureMessage("Sample app launched", level = "info")
        AppLogger.log("captureMessage(launch) eventId=$launchEventId")

        findViewById<Button>(R.id.btn_crash).setOnClickListener {
            AppLogger.log("User tapped Crash – throwing uncaught exception")
            XrayRadar.addBreadcrumb(message = "User triggered crash", type = "user", category = "test")
            throw RuntimeException("Test crash from XrayRadar sample app")
        }

        findViewById<Button>(R.id.btn_capture_exception).setOnClickListener {
            val ex = IllegalStateException("Test exception captured by SDK")
            val eventId = XrayRadar.captureException(ex, level = "error", message = "Button-triggered test exception")
            AppLogger.log("captureException eventId=$eventId")
            Toast.makeText(this, "Exception captured, eventId=$eventId", Toast.LENGTH_LONG).show()
        }

        findViewById<Button>(R.id.btn_capture_message).setOnClickListener {
            val eventId = XrayRadar.captureMessage("Test message from sample app", level = "info")
            AppLogger.log("captureMessage eventId=$eventId")
            Toast.makeText(this, "Message captured, eventId=$eventId", Toast.LENGTH_LONG).show()
        }

        findViewById<Button>(R.id.btn_add_breadcrumb).setOnClickListener {
            val msg = "Manual breadcrumb at ${System.currentTimeMillis()}"
            XrayRadar.addBreadcrumb(message = msg, type = "default", category = "test")
            AppLogger.log("addBreadcrumb: $msg")
            Toast.makeText(this, "Breadcrumb added", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_flush).setOnClickListener {
            AppLogger.log("Flush triggered – worker enqueued; [API] logs will appear below when send runs (often within seconds)")
            XrayRadar.flush()
            Toast.makeText(this, "Flush triggered – watch App log for [API] lines", Toast.LENGTH_LONG).show()
        }

        findViewById<Button>(R.id.btn_network).setOnClickListener {
            makeTestRequest()
        }
    }

    override fun onDestroy() {
        AppLogger.listener = null
        super.onDestroy()
    }

    private fun makeTestRequest() {
        AppLogger.log("makeTestRequest: starting HTTP request to httpbin.org")
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(this@MainActivity, "Sending request…", Toast.LENGTH_SHORT).show()
            val result = withContext(Dispatchers.IO) {
                try {
                    val request = Request.Builder()
                        .url("https://httpbin.org/get")
                        .build()
                    val response = httpClient.newCall(request).execute()
                    AppLogger.log("makeTestRequest: HTTP ${response.code} ${response.message}")
                    "HTTP ${response.code}"
                } catch (e: IOException) {
                    AppLogger.log("makeTestRequest failed: ${e.message}")
                    "Error: ${e.message}"
                }
            }
            Toast.makeText(this@MainActivity, result, Toast.LENGTH_SHORT).show()
        }
    }
}
