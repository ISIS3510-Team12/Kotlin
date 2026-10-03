package com.team12kotlin.juggle.data.repository

import com.team12kotlin.juggle.data.remote.JuggleApi
import com.team12kotlin.juggle.ui.dto.Group
import com.team12kotlin.juggle.ui.dto.GroupCreateRequest
import com.team12kotlin.juggle.ui.dto.GroupMemberAddRequest
import com.team12kotlin.juggle.ui.dto.GroupUpdateRequest

class GroupRepository(
    private val api: JuggleApi
) {
    suspend fun getGroups(): List<Group> = api.getGroups()

    suspend fun getGroup(groupId: Int): Group = api.getGroup(groupId)

    suspend fun createGroup(name: String, description: String): Group =
        api.createGroup(GroupCreateRequest(name = name, description = description))

    suspend fun updateGroup(groupId: Int, name: String, description: String): Group =
        api.updateGroup(groupId, GroupUpdateRequest(name = name, description = description))

    suspend fun addMember(groupId: Int, email: String): Group =
        api.addGroupMember(groupId, GroupMemberAddRequest(email = email))

    suspend fun removeMember(groupId: Int, memberUserId: String) {
        api.removeGroupMember(groupId, memberUserId)
    }

    suspend fun leaveGroup(groupId: Int) {
        api.leaveGroup(groupId)
    }
}
