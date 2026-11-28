package com.team12kotlin.juggle.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "spin_history",
    foreignKeys = [
        ForeignKey(
            entity = WheelEntity::class,
            parentColumns = ["wheelId"],
            childColumns = ["wheelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("wheelId")]
)
data class SpinHistoryEntity(
    @PrimaryKey(autoGenerate = true) val spinId: Int = 0,
    val wheelId: Int,
    val selectedOptionText: String,
    val spunAt: Long,
)
