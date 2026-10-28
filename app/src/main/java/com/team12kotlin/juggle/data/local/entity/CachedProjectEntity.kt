package com.team12kotlin.juggle.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "cached_projects",
    primaryKeys = ["userId", "projectId"],
    indices = [Index("userId", "groupId")]
)
data class CachedProjectEntity(
    val userId: String,
    val projectId: Int,
    val groupId: Int,
    val name: String,
    val description: String,
    val deadline: String?,
)
