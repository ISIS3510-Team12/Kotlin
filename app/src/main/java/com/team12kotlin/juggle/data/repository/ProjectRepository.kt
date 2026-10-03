package com.team12kotlin.juggle.data.repository

import com.team12kotlin.juggle.data.remote.JuggleApi
import com.team12kotlin.juggle.ui.dto.Project
import com.team12kotlin.juggle.ui.dto.ProjectCreateRequest
import com.team12kotlin.juggle.ui.dto.ProjectDeadlinePrediction
import com.team12kotlin.juggle.ui.dto.Task

class ProjectRepository(
    private val api: JuggleApi
) {
    suspend fun getProjects(groupId: Int): List<Project> = api.getProjects(groupId)

    suspend fun getProject(projectId: Int): Project = api.getProject(projectId)

    suspend fun createProject(request: ProjectCreateRequest): Project = api.createProject(request)

    suspend fun getDeadlinePrediction(projectId: Int): ProjectDeadlinePrediction =
        api.getDeadlinePrediction(projectId)
}
