package com.team12kotlin.juggle.data.repository

import com.team12kotlin.juggle.data.remote.JuggleApi
import com.team12kotlin.juggle.ui.dto.TaskNotification

class NotificationRepository(
    private val api: JuggleApi
) {
    suspend fun getNotifications(): List<TaskNotification> = api.getNotifications()
}
