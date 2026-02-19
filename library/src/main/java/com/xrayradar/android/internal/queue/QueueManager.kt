package com.xrayradar.android.internal.queue

import android.content.Context
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.xrayradar.android.internal.db.PendingEventEntity
import com.xrayradar.android.internal.db.XrayRadarDatabase
import com.xrayradar.android.internal.model.EventPayload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class QueueManager(
    private val appContext: Context,
    private val dsn: String,
    private val token: String,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { encodeDefaults = true }
    private val db by lazy {
        Room.databaseBuilder(
            appContext,
            XrayRadarDatabase::class.java,
            SendEventsWorker.DB_NAME,
        ).build()
    }

    fun enqueue(event: EventPayload) {
        scope.launch {
            db.pendingEventDao().insert(
                PendingEventEntity(
                    payload = json.encodeToString(event),
                    attempts = 0,
                    nextAttemptAt = 0L,
                ),
            )
        }
    }

    fun enqueueBlocking(event: EventPayload) {
        val payload = json.encodeToString(event)
        val localDb = Room.databaseBuilder(
            appContext,
            XrayRadarDatabase::class.java,
            SendEventsWorker.DB_NAME,
        ).build()
        try {
            kotlinx.coroutines.runBlocking(Dispatchers.IO) {
                localDb.pendingEventDao().insert(
                    PendingEventEntity(payload = payload, attempts = 0, nextAttemptAt = 0L),
                )
            }
        } finally {
            localDb.close()
        }
    }

    fun triggerFlush() {
        val request = OneTimeWorkRequestBuilder<SendEventsWorker>()
            .setInputData(SendEventsWorker.buildInputData(dsn, token))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()

        WorkManager.getInstance(appContext)
            .enqueueUniqueWork("xrayradar-send-events", ExistingWorkPolicy.KEEP, request)
    }
}
