package com.team12kotlin.juggle.data.local

import android.content.Context
import com.team12kotlin.juggle.ui.dto.TaskNotification
import com.team12kotlin.juggle.ui.dto.User
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ProfileCache(context: Context) {

    private val file = File(context.filesDir, "profile.json")
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun save(user: User) {
        runCatching { file.writeText(json.encodeToString(user)) }
    }

    fun read(): User? =
        runCatching { json.decodeFromString<User>(file.readText()) }.getOrNull()

    fun clear() {
        file.delete()
    }
}

class NotificationsCache(context: Context) {

    private val file = File(context.filesDir, "notifications.json")
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun save(items: List<TaskNotification>) {
        runCatching { file.writeText(json.encodeToString(items)) }
    }

    fun read(): List<TaskNotification>? =
        if (!file.exists()) null else runCatching {
            json.decodeFromString<List<TaskNotification>>(file.readText())
        }.getOrNull()

    fun clear() {
        file.delete()
    }
}
