package com.team12kotlin.juggle.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.team12kotlin.juggle.MainActivity
import com.team12kotlin.juggle.R
import com.team12kotlin.juggle.ui.dto.TaskTodaySummary

object TaskReminderNotifications {

    fun canNotify(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    fun show(
        context: Context,
        summary: TaskTodaySummary,
        channelId: String,
        channelName: String,
        channelDescription: String,
        notificationId: Int
    ) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = channelDescription
            }
        )
        val openApp = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Your tasks are waiting")
            .setContentText(message(summary))
            .setStyle(NotificationCompat.BigTextStyle().bigText(message(summary)))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        manager.notify(notificationId, notification)
    }

    fun message(summary: TaskTodaySummary): String {
        fun tasks(count: Int) = if (count == 1) "1 task" else "$count tasks"
        return when {
            summary.overdueCount == 0 ->
                "Remember you have ${tasks(summary.todayCount)} pending for today."
            summary.todayCount == 0 ->
                "Remember you have ${tasks(summary.overdueCount)} overdue and still pending."
            else -> "Remember you have ${summary.pendingCount} pending: " +
                "${summary.todayCount} for today and ${summary.overdueCount} overdue."
        }
    }
}
