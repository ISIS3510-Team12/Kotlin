package com.team12kotlin.juggle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.team12kotlin.juggle.data.local.entity.CachedGroupEntity

@Dao
interface CachedGroupDao {
    @Query("""
        SELECT * FROM cached_groups
        WHERE userId = :userId
        ORDER BY name
    """)
    suspend fun getGroups(userId: String): List<CachedGroupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(groups: List<CachedGroupEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOne(group: CachedGroupEntity)

    @Query("DELETE FROM cached_groups WHERE userId = :userId")
    suspend fun deleteForUser(userId: String)

    @Query("SELECT * FROM cached_groups WHERE userId = :userId AND groupId = :groupId")
    suspend fun getGroup(userId: String, groupId: Int): CachedGroupEntity?

    @Query("DELETE FROM cached_groups WHERE userId = :userId AND groupId = :groupId")
    suspend fun deleteGroup(userId: String, groupId: Int)

    @Transaction
    suspend fun replaceForUser(userId: String, groups: List<CachedGroupEntity>) {
        deleteForUser(userId)
        insertAll(groups)
    }
}