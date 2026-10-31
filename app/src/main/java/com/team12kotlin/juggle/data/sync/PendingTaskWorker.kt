package com.team12kotlin.juggle.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.team12kotlin.juggle.data.Dependencies

class PendingTaskWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val store = Dependencies.pendingTaskStore
        val currentUserId = Dependencies.authRepository.currentUser?.uid ?: return Result.success()
        val pending = store.all().filter { it.userId == currentUserId || it.userId.isEmpty() }
        if (pending.isEmpty()) return Result.success()
        val repository = Dependencies.taskRepository

        var failed = false
        for (task in pending) {
            runCatching { repository.syncPendingTask(task) }
                .onSuccess { store.remove(task.taskId) }
                .onFailure { failed = true }
        }

        store.notifySynced()
        return if (failed) Result.retry() else Result.success()
    }
}
