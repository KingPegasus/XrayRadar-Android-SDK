package com.xrayradar.android.internal.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PendingEventEntity::class],
    version = 1,
    exportSchema = false,
)
internal abstract class XrayRadarDatabase : RoomDatabase() {
    abstract fun pendingEventDao(): PendingEventDao
}
