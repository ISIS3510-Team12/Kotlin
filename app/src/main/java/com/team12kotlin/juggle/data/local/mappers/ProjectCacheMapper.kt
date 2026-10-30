package com.team12kotlin.juggle.data.local.mappers

import com.team12kotlin.juggle.data.local.entity.CachedProjectEntity
import com.team12kotlin.juggle.ui.dto.Project
import kotlinx.serialization.SerializationException
import kotlin.Int

class ProjectCacheMapper {
    fun toEntity(userId: String, project: Project): CachedProjectEntity =
        CachedProjectEntity(
            userId = userId,
            projectId = project.id,
            groupId = project.groupId,
            name = project.name,
            description = project.description,
            deadline = project.deadline
        )

    private fun toDomain(entity: CachedProjectEntity): Project =
        Project(
            id = entity.projectId,
            groupId = entity.groupId,
            name = entity.name,
            description = entity.description,
            deadline = entity.deadline
        )

    fun toDomainOrNull(entity: CachedProjectEntity): Project? =
        try {
            toDomain(entity)
        } catch (e: SerializationException) {
            null
        }
}