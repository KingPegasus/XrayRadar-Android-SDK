package com.xrayradar.android.internal.queue

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import androidx.work.Data
import androidx.room.Room
import com.xrayradar.android.internal.db.XrayRadarDatabase
import com.xrayradar.android.internal.model.EventPayload
import com.xrayradar.android.internal.transport.HttpTransport
import com.xrayradar.android.internal.transport.SendResult
import com.xrayradar.android.internal.transport.TransportDebugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

internal class SendEventsWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val dsn = inputData.getString(KEY_DSN) ?: run {
            TransportDebugLog.log("SendEventsWorker: missing DSN")
            return@withContext Result.failure()
        }
        val token = inputData.getString(KEY_TOKEN) ?: run {
            TransportDebugLog.log("SendEventsWorker: missing token")
            return@withContext Result.failure()
        }
        TransportDebugLog.log("SendEventsWorker: started")
        val db = Room.databaseBuilder(
            applicationContext,
            XrayRadarDatabase::class.java,
            DB_NAME,
        ).build()
        val dao = db.pendingEventDao()
        val transport = HttpTransport(dsn = dsn, authToken = token)
        val json = Json { ignoreUnknownKeys = true }

        try {
            val due = dao.getDue(System.currentTimeMillis(), 20)
            TransportDebugLog.log("SendEventsWorker: ${due.size} events due")
            val successIds = mutableListOf<Long>()

            for (row in due) {
                val event = try {
                    json.decodeFromString<EventPayload>(row.payload)
                } catch (e: Exception) {
                    TransportDebugLog.log("SendEventsWorker: skip invalid payload id=${row.id} ${e.message}")
                    successIds.add(row.id)
                    continue
                }
                when (val result = transport.send(event)) {
                    is SendResult.Success -> successIds.add(row.id)
                    is SendResult.Failure -> {
                        TransportDebugLog.log("SendEventsWorker: event ${event.eventId} failed (non-retryable), dropping")
                        successIds.add(row.id)
                    }
                    is SendResult.Retryable -> {
                        val attempts = row.attempts + 1
                        val waitMs = nextBackoffMs(attempts, result.retryAfterSeconds)
                        dao.markRetry(
                            id = row.id,
                            attempts = attempts,
                            nextAttemptAt = System.currentTimeMillis() + waitMs,
                        )
                        TransportDebugLog.log("SendEventsWorker: event ${event.eventId} retryable, attempts=$attempts nextIn=${waitMs}ms")
                    }
                }
            }
            if (successIds.isNotEmpty()) {
                dao.deleteIds(successIds)
                TransportDebugLog.log("SendEventsWorker: deleted ${successIds.size} sent events")
            }
            TransportDebugLog.log("SendEventsWorker: finished")
            Result.success()
        } finally {
            db.close()
        }
    }

    companion object {
        const val KEY_DSN = "dsn"
        const val KEY_TOKEN = "token"
        const val DB_NAME = "xrayradar.db"

        fun buildInputData(dsn: String, token: String): Data {
            return workDataOf(
                KEY_DSN to dsn,
                KEY_TOKEN to token,
            )
        }
    }
}
