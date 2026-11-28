package com.team12kotlin.juggle.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.team12kotlin.juggle.R
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.ui.tools.pomodoro.PomodoroPhase
import com.team12kotlin.juggle.ui.tools.pomodoro.PomodoroUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PomodoroService : Service() {
    private  val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        val manager = Dependencies.pomodoroTimerManager

        ServiceCompat.startForeground(
            this, NOTIF_ID, buildNotification(manager.uiState.value),
            if (Build.VERSION.SDK_INT >= 34) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else 0
        )

        scope.launch {
            manager.uiState.collect { state ->
                if (!state.isRunning) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    getSystemService(NotificationManager::class.java).notify(NOTIF_ID, buildNotification(state))
                }
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun buildNotification(state: PomodoroUiState): Notification {
        val time = "%02d:%02d".format(state.remainingSeconds / 60, state.remainingSeconds % 60)
        val title = if (state.phase == PomodoroPhase.BREAK) "Break" else "Focus"

        val openApp = PendingIntent.getActivity(
            this, 0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Pomodoro · $title")
            .setContentText(time)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openApp)
            .build()
    }

    private fun createChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Pomodoro timer", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "pomodoro_timer"
        private const val NOTIF_ID = 1001

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, PomodoroService::class.java))
        }
    }
}