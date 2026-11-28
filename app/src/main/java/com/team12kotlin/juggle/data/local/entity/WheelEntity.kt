package com.team12kotlin.juggle.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wheels",
    indices = [Index("userId")]
)
data class WheelEntity(
    @PrimaryKey(autoGenerate = true) val wheelId: Int = 0,
    val userId: String,
    val name: String,
    val createdAt: Long,
)
