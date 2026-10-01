package com.team12kotlin.juggle.data.repository

import com.team12kotlin.juggle.data.remote.JuggleApi
import com.team12kotlin.juggle.ui.dto.Group

class GroupRepository(
    private val api: JuggleApi
) {
    suspend fun getGroups(): List<Group> = api.getGroups()

    suspend fun getGroup(groupId: Int): Group = api.getGroup(groupId)
}
