package com.team12kotlin.juggle.data.repository

import com.team12kotlin.juggle.data.local.CacheResult
import com.team12kotlin.juggle.data.local.EvidenceCache
import com.team12kotlin.juggle.data.local.NetworkFirstCache
import com.team12kotlin.juggle.data.local.dao.CachedTaskDao
import com.team12kotlin.juggle.data.local.mappers.TaskCacheMapper
import com.team12kotlin.juggle.data.remote.JuggleApi
import com.team12kotlin.juggle.ui.dto.Reminder
import com.team12kotlin.juggle.ui.dto.ReminderRequest
import com.team12kotlin.juggle.ui.dto.ReminderUpdateRequest
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskCreateRequest
import com.team12kotlin.juggle.ui.dto.TaskStatus
import com.team12kotlin.juggle.ui.dto.TaskTodaySummary
import com.team12kotlin.juggle.ui.dto.TaskUpdateRequest
import com.team12kotlin.juggle.ui.dto.TimeBlock
import com.team12kotlin.juggle.ui.dto.TimeBlockRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.time.LocalDate

class TaskRepository(
    private val api: JuggleApi,
    private val taskDao: CachedTaskDao,
    private val mapper: TaskCacheMapper,
    private val cache: NetworkFirstCache,
    private val evidenceCache: EvidenceCache,
    private val userId: String
) {
    suspend fun getTasks(): List<Task> =
        try {
            api.getTasks()
        } catch (e: IOException) {
            getLocalAll(userId)
        }

    suspend fun getOwnTasks(groupId: Int): List<Task> = getOwnTasksCached(groupId).data

    suspend fun getOwnTasksCached(groupId: Int): CacheResult<List<Task>> =
        cache.fetch(
            userId = userId,
            resource = "ownTasks",
            scopeId = groupId.toString(),
            remote = { api.getOwnTasks(groupId) },
            saveLocal = { tasks ->
                taskDao.insertAll(
                    tasks.map { mapper.toEntity(userId, it).copy(groupId = groupId) }
                )
            },
            readLocal = {
                getLocalByGroup(userId, groupId).filter { it.isOwn(userId) }
            }
        )

    suspend fun getGroupTasks(groupId: Int): List<Task> = getGroupTasksCached(groupId).data

    suspend fun getGroupTasksCached(groupId: Int): CacheResult<List<Task>> {
        

        return cache.fetch(
            userId = userId,
            resource = "tasks",
            scopeId = groupId.toString(),
            remote = { api.getGroupTasks(groupId) },
            saveLocal = { tasks ->
                taskDao.replaceForGroup(
                    userId,
                    groupId,
                    tasks.map { mapper.toEntity(userId, it).copy(groupId = groupId) }
                )
            },
            readLocal = {
                taskDao.getByGroup(userId, groupId).mapNotNull { mapper.toDomainOrNull(it) }
            }
        )
    }

    suspend fun getAllTasks(
        dueWithinDays: Int? = null,
        mine: Boolean = false,
        priority: Boolean = false,
        startDate: String? = null,
        endDate: String? = null
    ): List<Task> =
        getAllTasksCached(
        dueWithinDays,
        mine,
        priority,
        startDate,
        endDate
    ).data

    suspend fun getAllTasksCached(
        dueWithinDays: Int? = null,
        mine: Boolean = false,
        priority: Boolean = false,
        startDate: String? = null,
        endDate: String? = null
    ): CacheResult<List<Task>> {
        

        return cache.fetch(
            userId = userId,
            resource = "tasks",
            scopeId = userId,
            remote = { api.getAllTasks(dueWithinDays, mine, priority, startDate, endDate) },
            saveLocal = { tasks ->
                taskDao.insertAll(tasks.map { mapper.toEntity(userId, it) })
            },
            readLocal = {
                getLocalAll(userId).filter { task ->
                    (!mine || task.isOwn(userId)) &&
                            (!priority || task.isPriority) &&
                            task.matchesDates(dueWithinDays, startDate, endDate)

                }
            }
        )
    }

    suspend fun getTodaySummary(start: String, end: String): TaskTodaySummary =
        api.getTodaySummary(start, end)

    suspend fun getTask(taskId: Int): Task {
        val task = try {
            api.getTask(taskId)
        } catch (e: IOException) {
            taskDao.getById(userId, taskId).let { mapper.toDomainOrNull(it) } ?:
            throw e
        }
        if (!task.hasPhoto) {
            withContext(Dispatchers.IO) { evidenceCache.remove(taskId) }
        }
        return task
    }

    suspend fun createTask(request: TaskCreateRequest): Task =
        api.createTask(request)
            .also { saveOne(userId, it) }

    suspend fun updateTask(taskId: Int, request: TaskUpdateRequest): Task =
        api.updateTask(taskId, request)
            .also { saveOne(userId, it) }

    suspend fun changeStatus(taskId: Int, status: TaskStatus): Task =
        api.changeTaskStatus(taskId, status.serializedName)
            .also { saveOne(userId, it) }

    suspend fun deleteTask(taskId: Int) {
        api.deleteTask(taskId)
        taskDao.deleteTask(userId, taskId)
        withContext(Dispatchers.IO) { evidenceCache.remove(taskId) }
    }

    suspend fun uploadTaskPhoto(taskId: Int, content: ByteArray, mimeType: String) {
        val body = content.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", "evidence", body)
        api.uploadTaskPhoto(taskId, part)
        withContext(Dispatchers.IO) { evidenceCache.put(taskId, content) }
        refreshTask(taskId)
    }

    suspend fun getTaskPhoto(taskId: Int): ByteArray? = withContext(Dispatchers.IO) {
        runCatching { api.getTaskPhoto(taskId).bytes() }
            .onSuccess { evidenceCache.put(taskId, it) }
            .getOrElse { evidenceCache.get(taskId) }
    }

    // --- Reminders -------------------------------------------------------

    suspend fun getReminders(taskId: Int): List<Reminder> =
        try {
            api.getReminders(taskId)
        } catch (e: IOException) {
            val cachedTask = taskDao.getById(userId, taskId)
                .let { mapper.toDomainOrNull(it) }
            cachedTask?.reminders ?: throw e
        }

    suspend fun createReminder(taskId: Int, request: ReminderRequest): Reminder =
        api.createReminder(taskId, request)
            .also { refreshTask(taskId) }

    suspend fun setReminderEnabled(taskId: Int, reminderId: Int, enabled: Boolean): Reminder =
        api.updateReminder(taskId, reminderId, ReminderUpdateRequest(enabled = enabled))
            .also { refreshTask(taskId) }

    // --- Time blocks -----------------------------------------------------

    suspend fun getTimeBlocks(taskId: Int): List<TimeBlock> = api.getTimeBlocks(taskId)

    suspend fun createTimeBlock(taskId: Int, request: TimeBlockRequest): TimeBlock =
        api.createTimeBlock(taskId, request)

    private suspend fun saveOne(userId: String,task: Task) {
        taskDao.insertOne(mapper.toEntity(userId, task))
    }

    private suspend fun refreshTask(taskId: Int) {
        runCatching { api.getTask(taskId) }
            .onSuccess { saveOne(userId, it) }
    }

    private suspend fun getLocalAll(userId: String) =
        taskDao.getAll(userId).mapNotNull { mapper.toDomainOrNull(it) }

    private suspend fun getLocalByGroup(userId: String, groupId: Int) =
        taskDao.getByGroup(userId, groupId).mapNotNull { mapper.toDomainOrNull(it) }

    private fun Task.isOwn(userId: String): Boolean =
       assignees.any { it.userId == userId }   // ajusta al criterio del servidor

    private fun Task.matchesDates(dueWithinDays: Int?, start: String?, end: String?): Boolean {
        if (dueWithinDays == null && start == null && end == null) return true
        val due = deadline?.let(::parseDate) ?: return false   // sin deadline: se excluye
        val today = LocalDate.now()
        return (dueWithinDays == null || due in today..today.plusDays(dueWithinDays.toLong())) &&
                (start == null || !due.isBefore(parseDate(start))) &&
                (end == null || !due.isAfter(parseDate(end)))
    }

    private fun parseDate(value: String): LocalDate = LocalDate.parse(value.take(10))
}
