package com.team12kotlin.juggle.data.repository

import com.team12kotlin.juggle.data.auth.UsserSession
import com.team12kotlin.juggle.data.local.CacheResult
import com.team12kotlin.juggle.data.local.NetworkFirstCache
import com.team12kotlin.juggle.data.local.dao.CachedProjectDao
import com.team12kotlin.juggle.data.local.mappers.ProjectCacheMapper
import com.team12kotlin.juggle.data.remote.JuggleApi
import com.team12kotlin.juggle.ui.dto.Project
import com.team12kotlin.juggle.ui.dto.ProjectCreateRequest
import com.team12kotlin.juggle.ui.dto.ProjectDeadlinePrediction
import java.io.IOException

class ProjectRepository(
    private val api: JuggleApi,
    private val projectDao: CachedProjectDao,
    private val mapper: ProjectCacheMapper,
    private val cache: NetworkFirstCache,
    private val session: UsserSession
) {
    suspend fun getProjects(groupId: Int): List<Project> = getProjectsCached(groupId).data

    suspend fun getProjectsCached(groupId: Int): CacheResult<List<Project>> {
        val userId = session.currentUserId()

        return cache.fetch(
            userId = userId,
            resource = "projects",
            scopeId = groupId.toString(),
            remote = { api.getProjects(groupId) },
            saveLocal = { projects ->
                projectDao.replaceForGroup(userId, groupId, projects.map { mapper.toEntity(userId, it) })
            },
            readLocal = {
                projectDao.getByGroup(userId, groupId).mapNotNull { mapper.toDomainOrNull(it) }
            }
        )
    }

    suspend fun getProject(projectId: Int): Project {
        val userId = session.currentUserId()

        return try {
            api.getProject(projectId).also { saveOne(userId, it) }
        } catch (e: IOException) {
            projectDao.getProject(userId, projectId)?.let { mapper.toDomainOrNull(it) }
                ?: throw e
        }
    }

    suspend fun createProject(request: ProjectCreateRequest): Project =
        api.createProject(request)
            .also { saveOne(session.currentUserId(), it) }

    suspend fun getDeadlinePrediction(projectId: Int): ProjectDeadlinePrediction =
        api.getDeadlinePrediction(projectId)

    private suspend fun saveOne(userId: String, project: Project) {
        projectDao.insertOne(mapper.toEntity(userId, project))
    }
}
