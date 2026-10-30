package com.team12kotlin.juggle.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.team12kotlin.juggle.data.Dependencies
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout
import java.time.LocalDate

class ScheduledReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        if (!ScheduledReminderStore(context).enabled) return Result.success()
        if (!TaskReminderNotifications.canNotify(context)) return Result.success()
        if (Dependencies.authRepository.currentUser == null) return Result.success()

        val today = LocalDate.now()
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (preferences.getString(KEY_LAST_NOTIFIED, null) == today.toString()) {
            return Result.success()
        }

        val summary = try {
            withTimeout(FETCH_TIMEOUT_MS) {
                Dependencies.taskRepository.getTodaySummary(
                    start = today.atStartOfDay().toString(),
                    end = today.atTime(23, 59, 59).toString()
                )
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            return Result.retry()
        }

        if (summary.pendingCount == 0) return Result.success()

        TaskReminderNotifications.show(
            context = context,
            summary = summary,
            channelId = CHANNEL_ID,
            channelName = "Scheduled reminders",
            channelDescription = "Reminder of your pending tasks at the time you choose",
            notificationId = NOTIFICATION_ID
        )
        preferences.edit().putString(KEY_LAST_NOTIFIED, today.toString()).apply()
        return Result.success()
    }

    private companion object {
        const val PREFS_NAME = "scheduled_reminders"
        const val KEY_LAST_NOTIFIED = "last_notified_date"
        const val CHANNEL_ID = "scheduled_task_reminders"
        const val NOTIFICATION_ID = 1002
        const val FETCH_TIMEOUT_MS = 20_000L
    }
}
