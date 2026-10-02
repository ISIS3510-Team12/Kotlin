package com.team12kotlin.juggle.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.team12kotlin.juggle.MainActivity
import com.team12kotlin.juggle.R
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.ui.dto.TaskTodaySummary
import kotlinx.coroutines.withTimeout
import java.time.LocalDate

private const val TAG = "LocationReminder"
private const val CHANNEL_ID = "task_reminders"
private const val NOTIFICATION_ID = 1001
private const val PREFS_NAME = "location_reminders"
private const val KEY_LAST_NOTIFIED = "last_notified_date"
private const val FETCH_TIMEOUT_MS = 20_000L

/** Tells the user, from the notification bar, how many tasks are pending today when they arrive at their saved place. */
object LocationReminderNotifier {

    /**
     * Fetches today's summary once and notifies if there is something pending.
     * At most one reminder per day, so re-entering the area doesn't spam.
     */
    suspend fun remindIfNeeded(context: Context) {
        if (!canNotify(context)) {
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
        show(context, summary)
        Log.d(TAG, "reminded: ${summary.pendingCount} pending")
        prefs.edit().putString(KEY_LAST_NOTIFIED, today.toString()).apply()
    }

    fun reset(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }

    private fun canNotify(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun show(context: Context, summary: TaskTodaySummary) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Task reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Reminders of your pending tasks when you get to your saved place"
            }
        )
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Your tasks are waiting")
            .setContentText(message(summary))
            .setStyle(NotificationCompat.BigTextStyle().bigText(message(summary)))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    internal fun message(summary: TaskTodaySummary): String {
        fun tasks(count: Int) = if (count == 1) "1 task" else "$count tasks"
        return when {
            summary.overdueCount == 0 -> "Remember you have ${tasks(summary.todayCount)} pending for today."
            summary.todayCount == 0 -> "Remember you have ${tasks(summary.overdueCount)} overdue and still pending."
            else -> "Remember you have ${tasks(summary.pendingCount)} pending: " +
                "${summary.todayCount} for today and ${summary.overdueCount} overdue."
        }
    }
}
