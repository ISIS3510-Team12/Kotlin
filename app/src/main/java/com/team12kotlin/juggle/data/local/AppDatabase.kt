package com.team12kotlin.juggle.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.team12kotlin.juggle.data.local.dao.CacheMetadataDao
import com.team12kotlin.juggle.data.local.dao.CachedGroupDao
import com.team12kotlin.juggle.data.local.dao.CachedProjectDao
import com.team12kotlin.juggle.data.local.dao.CachedTaskDao
import com.team12kotlin.juggle.data.local.entity.CacheMetadataEntity
import com.team12kotlin.juggle.data.local.entity.CachedGroupEntity
import com.team12kotlin.juggle.data.local.entity.CachedProjectEntity
import com.team12kotlin.juggle.data.local.entity.CachedTaskEntity

@Database(
    entities = [
        CacheMetadataEntity::class,
        CachedGroupEntity::class,
        CachedProjectEntity::class,
        CachedTaskEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun cachedMetadataDao(): CacheMetadataDao
    abstract fun cachedGroupDao(): CachedGroupDao
    abstract fun cachedProjectDao(): CachedProjectDao
    abstract fun cachedTaskDao(): CachedTaskDao
}

/** Adds the last change time of a task, so the cache keeps what it already holds. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE cached_tasks ADD COLUMN lastChangedAt TEXT")
    }
}