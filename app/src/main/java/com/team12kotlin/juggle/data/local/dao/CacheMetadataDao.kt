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
        SELECT * FROM cache_metadata
        WHERE userId = :userId AND cachedResource = :resource
        ORDER BY cachedAt
    """)
    suspend fun getMetadata(userId: String, resource: String): List<CacheMetadataEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOne(metadata: CacheMetadataEntity)

    @Query("DELETE FROM cache_metadata WHERE userId = :userId AND cachedResource = :resource")
    suspend fun deleteForUser(userId: String, resource: String)
}