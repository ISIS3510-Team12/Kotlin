package com.team12kotlin.juggle.data.local

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.team12kotlin.juggle.data.sync.PendingTaskWorker
import com.team12kotlin.juggle.ui.dto.TaskCreateRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.TimeUnit

@Serializable
data class PendingTask(
    val taskId: Int,
    val request: TaskCreateRequest,
    val photoPath: String? = null,
    val createdAt: Long = 0L
) {
    fun toTask(): com.team12kotlin.juggle.ui.dto.Task =
        com.team12kotlin.juggle.ui.dto.Task(
            id = taskId,
            title = request.title,
            taskType = request.taskType,
            status = com.team12kotlin.juggle.ui.dto.TaskStatus.NOT_STARTED,
            isPriority = request.isPriority,
            needsHelp = request.needsHelp,
            deadline = request.deadline,
            description = request.description.orEmpty(),
            projectId = request.projectId,
            groupId = request.groupId
        )
}

class PendingTaskStore(private val context: Context) {

    private val file = File(context.filesDir, "pending_tasks.json")
    private val photoDirectory = File(context.filesDir, "pending_photos").apply { mkdirs() }
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val mutex = Mutex()

    private val _synced = MutableStateFlow(0L)
    val synced: StateFlow<Long> = _synced.asStateFlow()

    fun notifySynced() {
        _synced.value = System.currentTimeMillis()
    }

    suspend fun all(): List<PendingTask> = mutex.withLock { read() }

    suspend fun pendingForGroup(groupId: Int): List<PendingTask> =
        all().filter { it.request.groupId == groupId }

    suspend fun enqueue(request: TaskCreateRequest, photo: ByteArray?): PendingTask {
        val pending = PendingTask(
            taskId = nextTemporaryId(),
            request = request,
            photoPath = photo?.let { savePhoto(it) },
            createdAt = System.currentTimeMillis()
        )
        mutex.withLock { write(read() + pending) }
        schedule()
        return pending
    }

    suspend fun find(taskId: Int): PendingTask? = all().firstOrNull { it.taskId == taskId }

    suspend fun update(taskId: Int, request: TaskCreateRequest, photo: ByteArray?) {
        mutex.withLock {
            write(
                read().map {
                    if (it.taskId == taskId) {
                        it.copy(
                            request = request,
                            photoPath = photo?.let { bytes -> savePhoto(bytes) } ?: it.photoPath
                        )
                    } else {
                        it
                    }
                }
            )
        }
        schedule()
    }

    suspend fun remove(taskId: Int) {
        mutex.withLock {
            val pending = read().firstOrNull { it.taskId == taskId }
            pending?.photoPath?.let { File(it).delete() }
            write(read().filterNot { it.taskId == taskId })
        }
    }

    suspend fun photo(path: String): ByteArray? =
        runCatching { File(path).readBytes() }.getOrNull()

    suspend fun clear() {
        mutex.withLock {
            file.delete()
            photoDirectory.listFiles()?.forEach { it.delete() }
        }
    }

    private fun savePhoto(bytes: ByteArray): String {
        val file = File(photoDirectory, "${System.currentTimeMillis()}.jpg")
        file.writeBytes(bytes)
        return file.absolutePath
    }

    private fun schedule() {
        val request = OneTimeWorkRequestBuilder<PendingTaskWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    private fun nextTemporaryId(): Int {
        val candidate = -(System.currentTimeMillis() % 1_000_000).toInt() - 1
        return if (read().any { it.taskId == candidate }) candidate - 1 else candidate
    }

    private fun read(): List<PendingTask> =
        runCatching { json.decodeFromString<List<PendingTask>>(file.readText()) }
            .getOrDefault(emptyList())

    private fun write(items: List<PendingTask>) {
        runCatching { file.writeText(json.encodeToString(items)) }
    }

    private companion object {
        const val WORK_NAME = "pending-tasks"
    }
}
