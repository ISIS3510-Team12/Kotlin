package com.team12kotlin.juggle.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wheel_options",
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
data class WheelOptionEntity(
    @PrimaryKey(autoGenerate = true) val optionId: Int = 0,
    val wheelId: Int,
    val optionText: String,
    val position: Int,
)
