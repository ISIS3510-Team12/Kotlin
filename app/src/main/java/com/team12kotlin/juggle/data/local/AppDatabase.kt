package com.team12kotlin.juggle.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
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
    version = 1,
    exportSchema = true
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun cachedMetadataDao(): CacheMetadataDao
    abstract fun cachedGroupDao(): CachedGroupDao
    abstract fun cachedProjectDao(): CachedProjectDao
    abstract fun cachedTaskDao(): CachedTaskDao
}