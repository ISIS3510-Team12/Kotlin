package com.team12kotlin.juggle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team12kotlin.juggle.data.local.entity.CacheMetadataEntity
import com.team12kotlin.juggle.data.local.entity.CachedGroupEntity

@Dao
interface CacheMetadataDao {

    @Query("""
        SELECT cachedAt FROM cache_metadata
        WHERE userId = :userId AND cachedResource = :resource AND scopeId = :scopeId
    """)
    suspend fun lastSyncedAt(userId: String, resource: String, scopeId: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOne(metadata: CacheMetadataEntity)

    @Query("DELETE FROM cache_metadata WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}