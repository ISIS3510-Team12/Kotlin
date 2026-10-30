package com.team12kotlin.juggle.data.local.mappers

import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.local.CacheConverter
import com.team12kotlin.juggle.data.local.entity.CachedTaskEntity
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskStatus
import kotlinx.serialization.SerializationException

class TaskCacheMapper(private val converter: CacheConverter = Dependencies.cacheConverter) {

    fun toEntity(userId: String, task: Task): CachedTaskEntity =
        CachedTaskEntity(
            userId = userId,
            taskId = task.id,
            projectId = task.projectId,
            groupId = task.groupId,
            title = task.title,
            taskType = task.taskType,
            status = task.status.serializedName,
            isPriority = task.isPriority,
            needsHelp = task.needsHelp,
            deadline = task.deadline,
            description = task.description,
            ownerId = task.ownerId,
            hasPhoto = task.hasPhoto,
            assigneesJson = converter.usersToJson(task.assignees),
            remindersJson = converter.remindersToJson(task.reminders),
            relatedTasksJson = converter.relatedTasksToJson(task.relatedTasks),
        )

    fun toDomain(entity: CachedTaskEntity): Task =
        Task(
            id = entity.taskId,
            projectId = entity.projectId,
            groupId = entity.groupId,
            title = entity.title,
            taskType = entity.taskType,
            status = toTaskStatus(entity.status),
            isPriority = entity.isPriority,
            needsHelp = entity.needsHelp,
            deadline = entity.deadline,
            description = entity.description,
            ownerId = entity.ownerId,
            hasPhoto = entity.hasPhoto,
            assignees = converter.usersFromJson(entity.assigneesJson),
            reminders = converter.remindersFromJson(entity.remindersJson),
            relatedTasks = converter.relatedTasksFromJson(entity.relatedTasksJson),
        )

    fun toDomainOrNull(entity: CachedTaskEntity): Task? =
        try {
            toDomain(entity)
        } catch (e: SerializationException) {
            null
        }

    private fun toTaskStatus(status: String): TaskStatus =
        when (status) {
            "completed" -> TaskStatus.COMPLETED
            "not started" -> TaskStatus.NOT_STARTED
            "in_progress" -> TaskStatus.IN_PROGRESS
            else -> throw IllegalArgumentException("Unknown cached task status: $this")
        }

}