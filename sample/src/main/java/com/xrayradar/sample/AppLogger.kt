package com.xrayradar.sample

import android.os.Handler
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * In-memory logger that can be displayed in the app UI.
 * Call [log] from anywhere; if a [Listener] is set (e.g. by MainActivity), the log is shown on screen.
 */
object AppLogger {
    private const val MAX_LINES = 300
    private val lines = mutableListOf<String>()
    private val lock = Any()
    private val mainHandler = Handler(Looper.getMainLooper())

    var listener: Listener? = null
        set(value) {
            field = value
            mainHandler.post { value?.onLogUpdated(getAllLines()) }
        }

    fun log(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        val line = "$timestamp  $message"
        synchronized(lock) {
            lines.add(line)
            if (lines.size > MAX_LINES) lines.removeAt(0)
        }
        android.util.Log.i("XrayRadarSample", message)
        val copy = getAllLines()
        mainHandler.post { listener?.onLogUpdated(copy) }
    }

    fun getAllLines(): String = synchronized(lock) { lines.joinToString("\n") }

    fun clear() {
        synchronized(lock) { lines.clear() }
        mainHandler.post { listener?.onLogUpdated("") }
    }

    interface Listener {
        fun onLogUpdated(fullText: String)
    }
}
