package com.team12kotlin.juggle.reminders

import android.content.Context
import android.util.Log
import com.team12kotlin.juggle.data.Dependencies
import kotlinx.coroutines.withTimeout
import java.time.LocalDate

private const val TAG = "LocationReminder"
private const val CHANNEL_ID = "task_reminders"
private const val NOTIFICATION_ID = 1001
private const val PREFS_NAME = "location_reminders"
private const val KEY_LAST_NOTIFIED = "last_notified_date"
private const val FETCH_TIMEOUT_MS = 20_000L

/** Shows the pending tasks reminder notification. */
object LocationReminderNotifier {

    /** Shows the reminder if there are pending tasks, at most once per day. */
    suspend fun remindIfNeeded(context: Context) {
        if (!TaskReminderNotifications.canNotify(context)) {
            Log.d(TAG, "skipped: notification permission not granted")
            return
        }
        val today = LocalDate.now()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getString(KEY_LAST_NOTIFIED, null) == today.toString()) {
            Log.d(TAG, "skipped: already reminded today")
            return
        }

        val summary = runCatching {
            withTimeout(FETCH_TIMEOUT_MS) {
                Dependencies.taskRepository.getTodaySummary(
                    start = today.atStartOfDay().toString(),
                    end = today.atTime(23, 59, 59).toString()
                )
            }
        }.getOrNull()
        if (summary == null) {
            Log.d(TAG, "skipped: couldn't fetch today's summary")
            return
        }
        if (summary.pendingCount == 0) {
            Log.d(TAG, "skipped: nothing pending")
            return
        }
        TaskReminderNotifications.show(
            context = context,
            summary = summary,
            channelId = CHANNEL_ID,
            channelName = "Task reminders",
            channelDescription = "Reminders of your pending tasks when you get to your saved place",
            notificationId = NOTIFICATION_ID
        )
        Log.d(TAG, "reminded: ${summary.pendingCount} pending")
        prefs.edit().putString(KEY_LAST_NOTIFIED, today.toString()).apply()
    }

    fun reset(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
