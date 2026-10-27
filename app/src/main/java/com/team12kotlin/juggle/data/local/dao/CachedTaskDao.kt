package com.team12kotlin.juggle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team12kotlin.juggle.data.local.entity.CachedTaskEntity

@Dao
interface CachedTaskDao {
    @Query("""
        SELECT * FROM cached_tasks
        WHERE userId = :userId AND groupId = :groupId
    """)
    suspend fun getTasks(userId: String, groupId: Int): List<CachedTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(groups: List<CachedTaskEntity>)

    @Query("DELETE FROM cached_tasks WHERE userId = :userId AND groupId = :groupId")
    suspend fun deleteForGroup(userId: String, groupId: Int)
}