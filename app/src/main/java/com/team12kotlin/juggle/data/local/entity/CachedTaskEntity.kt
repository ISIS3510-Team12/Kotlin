package com.team12kotlin.juggle.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "cached_tasks",
    primaryKeys = ["userId", "taskId"],
    indices = [Index("userId", "projectId")]
)
data class CachedTaskEntity(
    val userId: String,
    val taskId: Int,
    val projectId: Int?,
    val groupId: Int?,
    val title: String,
    val taskType: String,
    val status: String,
    val isPriority: Boolean,
    val needsHelp: Boolean,
    val deadline: String?,
    val description: String,
    val ownerId: String,
    val hasPhoto: Boolean,
    val assigneesJson: String,
    val remindersJson: String,
    val relatedTasksJson: String,
)
