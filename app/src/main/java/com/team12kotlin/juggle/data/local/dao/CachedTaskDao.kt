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
        WHERE userId = :userId AND taskId = :taskId
    """)
    suspend fun getById(userId: String, taskId: Int): CachedTaskEntity?

    @Query("""
        SELECT * FROM cached_tasks
        WHERE userId = :userId AND groupId = :groupId
    """)
    suspend fun getByGroup(userId: String, groupId: Int): List<CachedTaskEntity>

    @Query("""
        SELECT * FROM cached_tasks
        WHERE userId = :userId AND groupId = :groupId AND status = 'completed'
    """)
    suspend fun getCompletedByGroup(userId: String, groupId: Int): List<CachedTaskEntity>

    @Query("SELECT * FROM cached_tasks WHERE userId = :userId")
    suspend fun getAll(userId: String): List<CachedTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<CachedTaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOne(task: CachedTaskEntity)

    @Query("DELETE FROM cached_tasks WHERE userId = :userId AND taskId = :taskId")
    suspend fun deleteTask(userId: String, taskId: Int)

    @Query("DELETE FROM cached_tasks WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Query("DELETE FROM cached_tasks WHERE userId = :userId AND groupId = :groupId")
    suspend fun deleteForGroup(userId: String, groupId: Int)

    @Query("""
        DELETE FROM cached_tasks
        WHERE userId = :userId AND groupId = :groupId AND status != 'completed'
    """)
    suspend fun deleteOpenForGroup(userId: String, groupId: Int)

    @Query("""
        DELETE FROM cached_tasks
        WHERE userId = :userId AND groupId = :groupId AND status = 'completed'
    """)
    suspend fun deleteCompletedForGroup(userId: String, groupId: Int)

    /** Keeps the completed tasks: the group endpoints do not return them, so they would be lost. */
    @Transaction
    suspend fun replaceForGroup(
        userId: String,
        groupId: Int,
        tasks: List<CachedTaskEntity>,
    ) {
        deleteOpenForGroup(userId, groupId)
        insertAll(tasks)
    }

    @Transaction
    suspend fun replaceCompletedForGroup(
        userId: String,
        groupId: Int,
        tasks: List<CachedTaskEntity>,
    ) {
        deleteCompletedForGroup(userId, groupId)
        insertAll(tasks)
    }
}