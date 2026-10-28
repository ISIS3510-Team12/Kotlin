package com.team12kotlin.juggle.data.local.mappers

import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.local.CacheConverter
import com.team12kotlin.juggle.data.local.entity.CachedGroupEntity
import com.team12kotlin.juggle.ui.dto.Group
import kotlinx.serialization.SerializationException

class GroupCacheMapper(private val converter: CacheConverter = Dependencies.cacheConverter) {
    fun toEntity(userId: String, group: Group): CachedGroupEntity =
        CachedGroupEntity(
            userId = userId,
            groupId = group.id,
            name = group.name,
            description = group.description,
            deadline = group.deadline,
            usersJson = converter.usersToJson(group.users),
            pendingTaskCount = group.pendingTaskCount
        )

    private fun toDomain(entity: CachedGroupEntity): Group =
        Group(
            id = entity.groupId,
            name = entity.name,
            description = entity.description,
            deadline = entity.deadline,
            users = converter.usersFromJson(entity.usersJson),
            pendingTaskCount = entity.pendingTaskCount,
        )
    fun toDomainOrNull(entity: CachedGroupEntity): Group? =
        try {
            toDomain(entity)
        } catch (e: SerializationException) {
            null
        }

}