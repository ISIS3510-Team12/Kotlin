package com.team12kotlin.juggle.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.team12kotlin.juggle.data.local.entity.SpinHistoryEntity
import com.team12kotlin.juggle.data.local.entity.WheelEntity
import com.team12kotlin.juggle.data.local.entity.WheelOptionEntity
import kotlinx.coroutines.flow.Flow

data class WheelWithCount(
    @Embedded val wheel: WheelEntity,
    val optionCount: Int
)

@Dao
interface WheelDao {

    @Query("""
        SELECT w.*, COUNT(o.optionId) AS optionCount
        FROM wheels w
        LEFT JOIN wheel_options o ON o.wheelId = w.wheelId
        WHERE w.userId = :userId
        GROUP BY w.wheelId
        ORDER BY w.createdAt DESC
    """)
    fun observeWheels(userId: String): Flow<List<WheelWithCount>>

    @Query("SELECT * FROM wheels WHERE userId = :userId AND wheelId = :wheelId")
    suspend fun getWheel(userId: String, wheelId: Int): WheelEntity?

    @Insert
    suspend fun insertWheel(wheel: WheelEntity): Long

    @Query("UPDATE wheels SET name = :name WHERE wheelId = :wheelId")
    suspend fun renameWheel(wheelId: Int, name: String)

    @Query("DELETE FROM wheels WHERE wheelId = :wheelId")
    suspend fun deleteWheel(wheelId: Int)

    @Query("SELECT * FROM wheel_options WHERE wheelId = :wheelId ORDER BY position")
    suspend fun getOptions(wheelId: Int): List<WheelOptionEntity>

    @Insert
    suspend fun insertOptions(options: List<WheelOptionEntity>)

    @Query("UPDATE wheel_options SET optionText = :text WHERE optionId = :optionId")
    suspend fun updateOption(optionId: Int, text: String)

    @Query("DELETE FROM wheel_options WHERE optionId = :optionId")
    suspend fun deleteOption(optionId: Int)

    @Query("SELECT COALESCE(MAX(position), -1) FROM wheel_options WHERE wheelId = :wheelId")
    suspend fun lastPosition(wheelId: Int): Int

    @Insert
    suspend fun insertSpin(spin: SpinHistoryEntity)

    @Query("SELECT * FROM spin_history WHERE wheelId = :wheelId ORDER BY spunAt DESC")
    suspend fun getSpins(wheelId: Int): List<SpinHistoryEntity>

    @Transaction
    suspend fun createWheel(wheel: WheelEntity, options: List<String>): Int {
        val wheelId = insertWheel(wheel).toInt()
        insertOptions(options.mapIndexed { index, text ->
            WheelOptionEntity(wheelId = wheelId, optionText = text, position = index)
        })
        return wheelId
    }

    @Transaction
    suspend fun addOption(wheelId: Int, text: String) {
        insertOptions(
            listOf(WheelOptionEntity(wheelId = wheelId, optionText = text, position = lastPosition(wheelId) + 1))
        )
    }
}
