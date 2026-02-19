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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

internal class SendEventsWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val dsn = inputData.getString(KEY_DSN) ?: return@withContext Result.failure()
        val token = inputData.getString(KEY_TOKEN) ?: return@withContext Result.failure()
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
            val successIds = mutableListOf<Long>()

            for (row in due) {
                val event = try {
                    json.decodeFromString<EventPayload>(row.payload)
                } catch (_: Exception) {
                    successIds.add(row.id)
                    continue
                }
                when (val result = transport.send(event)) {
                    is SendResult.Success -> successIds.add(row.id)
                    is SendResult.Failure -> {
                        // non-retryable by default, drop bad payloads/auth errors
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
                    }
                }
            }
            if (successIds.isNotEmpty()) dao.deleteIds(successIds)
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
