package com.team12kotlin.juggle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.team12kotlin.juggle.data.local.entity.CachedTaskEntity

@Dao
interface CachedTaskDao {
    @Query("""
        SELECT * FROM cached_tasks
        WHERE userId = :userId AND groupId = :groupId
    """)
    suspend fun getByGroup(userId: String, groupId: Int): List<CachedTaskEntity>

    @Query("SELECT * FROM cached_tasks WHERE userId = :userId AND projectId = :projectId")
    suspend fun getByProject(userId: String, projectId: Int): List<CachedTaskEntity>

    @Query("SELECT * FROM cached_tasks WHERE userId = :userId")
    suspend fun getAll(userId: String): List<CachedTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(groups: List<CachedTaskEntity>)

    @Query("DELETE FROM cached_tasks WHERE userId = :userId AND groupId = :groupId")
    suspend fun deleteForGroup(userId: String, groupId: Int)

    @Query("DELETE FROM cached_tasks WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Transaction
    suspend fun replaceForGroup(
        userId: String,
        groupId: Int,
        tasks: List<CachedTaskEntity>,
    ) {
        deleteForGroup(userId, groupId)
        insertAll(tasks)
    }
}