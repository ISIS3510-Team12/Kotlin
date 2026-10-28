package com.team12kotlin.juggle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.team12kotlin.juggle.data.local.entity.CachedProjectEntity

@Dao
interface CachedProjectDao {
    @Query("""
        SELECT * FROM cached_projects
        WHERE userId = :userId
    """)
    suspend fun getProjects(userId: String): List<CachedProjectEntity>

    @Query("""
        SELECT * FROM cached_projects
        WHERE userId = :userId AND groupId = :groupId
    """)
    suspend fun getByGroup(userId: String, groupId: Int): List<CachedProjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(projects: List<CachedProjectEntity>)

    @Query("""
        DELETE FROM cached_projects
        WHERE userId = :userId AND groupId = :groupId
    """)
    suspend fun deleteForGroup(userId: String, groupId: Int)

    @Query("""
        DELETE FROM cached_projects
        WHERE userId = :userId
    """)
    suspend fun deletaAllForUser(userId: String)

    @Transaction
    suspend fun replaceForGroup(
        userId: String,
        groupId: Int,
        projects: List<CachedProjectEntity>,
    ) {
        deleteForGroup(userId, groupId)
        insertAll(projects)
    }
}