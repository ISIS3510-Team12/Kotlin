package com.team12kotlin.juggle.data.repository

import com.team12kotlin.juggle.data.remote.JuggleApi
import com.team12kotlin.juggle.ui.dto.Project

class ProjectRepository(
    private val api: JuggleApi
) {
    suspend fun getProjects(groupId: Int): List<Project> = api.getProjects(groupId)
}
