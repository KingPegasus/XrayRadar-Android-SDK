package com.xrayradar.android.internal.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_events")
internal data class PendingEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val payload: String,
    val attempts: Int = 0,
    val nextAttemptAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
)
