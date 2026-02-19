package com.xrayradar.android.internal.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
internal interface PendingEventDao {
    @Insert
    suspend fun insert(entity: PendingEventEntity): Long

    @Query("SELECT * FROM pending_events WHERE nextAttemptAt <= :nowMs ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getDue(nowMs: Long, limit: Int): List<PendingEventEntity>

    @Query("DELETE FROM pending_events WHERE id IN (:ids)")
    suspend fun deleteIds(ids: List<Long>)

    @Query("UPDATE pending_events SET attempts = :attempts, nextAttemptAt = :nextAttemptAt WHERE id = :id")
    suspend fun markRetry(id: Long, attempts: Int, nextAttemptAt: Long)
}
