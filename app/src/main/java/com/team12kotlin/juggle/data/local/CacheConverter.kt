package com.team12kotlin.juggle.data.local

import com.team12kotlin.juggle.ui.dto.RelatedTask
import com.team12kotlin.juggle.ui.dto.Reminder
import com.team12kotlin.juggle.ui.dto.User
import kotlinx.serialization.json.Json

class CacheConverter (private val json: Json) {
    fun usersToJson(users: List<User>): String =
        json.encodeToString(users)

    fun usersFromJson(value: String): List<User> =
        json.decodeFromString(value)

    fun remindersToJson(reminders: List<Reminder>): String =
        json.encodeToString(reminders)

    fun remindersFromJson(value: String): List<Reminder> =
        json.decodeFromString(value)

    fun relatedTasksToJson(tasks: List<RelatedTask>): String =
        json.encodeToString(tasks)

    fun relatedTasksFromJson(value: String): List<RelatedTask> =
        json.decodeFromString(value)
}