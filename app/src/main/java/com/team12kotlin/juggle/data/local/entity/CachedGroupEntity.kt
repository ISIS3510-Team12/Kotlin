package com.team12kotlin.juggle.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "cached_groups",
    primaryKeys = ["userId", "groupId"]
)
data class CachedGroupEntity(
    val userId: String,
    val groupId: Int,
    val name: String,
    val description: String,
    val deadline: String?,
    val usersJson: String,
    val pendingTaskCount: Int,
    val cachedAt: Long
)
