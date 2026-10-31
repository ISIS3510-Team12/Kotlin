package com.team12kotlin.juggle.data.repository

import com.team12kotlin.juggle.data.local.NotificationsCache
import com.team12kotlin.juggle.data.remote.JuggleApi
import com.team12kotlin.juggle.ui.dto.TaskNotification

class NotificationRepository(
    private val api: JuggleApi,
    private val cache: NotificationsCache
) {
    suspend fun getNotifications(): List<TaskNotification> =
        try {
            api.getNotifications().also { cache.save(it) }
        } catch (e: java.io.IOException) {
            val cached = cache.read() ?: throw e
            cached
        }
}
