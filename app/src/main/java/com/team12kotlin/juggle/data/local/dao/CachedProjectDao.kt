package com.team12kotlin.juggle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team12kotlin.juggle.data.local.entity.CachedProjectEntity

@Dao
interface CachedProjectDao {
    @Query("""
        SELECT * FROM cached_projects
        WHERE userId = :userId AND groupId = :groupId
        ORDER BY name
    """)
    suspend fun getProjects(userId: String, groupId: Int): List<CachedProjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(projects: List<CachedProjectEntity>)

    @Query("""
        DELETE FROM cached_projects
        WHERE userId = :userId AND groupId = :groupId
    """)
    suspend fun deleteForGroup(userId: String, groupId: Int)
}