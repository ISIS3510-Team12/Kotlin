package com.team12kotlin.juggle.data.repository

import com.team12kotlin.juggle.data.auth.UsserSession
import com.team12kotlin.juggle.data.local.CacheResult
import com.team12kotlin.juggle.data.local.NetworkFirstCache
import com.team12kotlin.juggle.data.local.dao.CachedGroupDao
import com.team12kotlin.juggle.data.local.dao.CachedProjectDao
import com.team12kotlin.juggle.data.local.dao.CachedTaskDao
import com.team12kotlin.juggle.data.local.mappers.GroupCacheMapper
import com.team12kotlin.juggle.data.remote.JuggleApi
import com.team12kotlin.juggle.ui.dto.Group
import com.team12kotlin.juggle.ui.dto.GroupCreateRequest
import com.team12kotlin.juggle.ui.dto.GroupMemberAddRequest
import com.team12kotlin.juggle.ui.dto.GroupUpdateRequest
import java.io.IOException

class GroupRepository(
    private val api: JuggleApi,
    private val groupDao: CachedGroupDao,
    private val projectDao: CachedProjectDao,
    private val taskDao: CachedTaskDao,
    private val mapper: GroupCacheMapper,
    private val cache: NetworkFirstCache,
    private val session: UsserSession
) {
    suspend fun getGroups(): List<Group> = getGroupsCached().data

    suspend fun getGroupsCached(): CacheResult<List<Group>> {
        val userId = session.currentUserId()

        return cache.fetch(
            userId = userId,
            resource = "groups",
            scopeId = userId,
            remote = {api.getGroups()},
            saveLocal = {groups ->
                groupDao.replaceForUser(userId, groups.map { mapper.toEntity(userId, it) })
            },
            readLocal = {
                groupDao.getGroups(userId).mapNotNull { mapper.toDomainOrNull(it) }
            }
        )
    }

    suspend fun getGroup(groupId: Int): Group {
        val userId = session.currentUserId()
        return try {
            api.getGroup(groupId).also { saveOne(userId, it) }
        } catch (e: IOException) {
            groupDao.getGroup(userId, groupId)?.let { mapper.toDomainOrNull(it) }
                ?: throw e
        }
    }

    suspend fun createGroup(name: String, description: String): Group =
        api.createGroup(GroupCreateRequest(name = name, description = description))
            .also { saveOne(session.currentUserId(), it) }

    suspend fun updateGroup(groupId: Int, name: String, description: String): Group =
        api.updateGroup(groupId, GroupUpdateRequest(name = name, description = description))
            .also { saveOne(session.currentUserId(), it) }

    suspend fun addMember(groupId: Int, email: String): Group =
        api.addGroupMember(groupId, GroupMemberAddRequest(email = email))
            .also { saveOne(session.currentUserId(), it) }

    suspend fun removeMember(groupId: Int, memberUserId: String) {
        api.removeGroupMember(groupId, memberUserId)
        runCatching { api.getGroup(groupId) }
            .onSuccess { saveOne(session.currentUserId(), it) }
    }

    suspend fun leaveGroup(groupId: Int) {
        api.leaveGroup(groupId)
        val userId = session.currentUserId()
        taskDao.deleteForGroup(userId, groupId)
        projectDao.deleteForGroup(userId, groupId)
        groupDao.deleteGroup(userId, groupId)
    }

    private suspend fun saveOne(userId: String, group: Group) {
        groupDao.insertOne(mapper.toEntity(userId, group))
    }
}
