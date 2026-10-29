package com.team12kotlin.juggle.reminders

import android.content.Context
import java.time.LocalTime

class ScheduledReminderStore(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = preferences.getBoolean(KEY_ENABLED, false)
        set(value) = preferences.edit().putBoolean(KEY_ENABLED, value).apply()

    var hour: Int
        get() = preferences.getInt(KEY_HOUR, DEFAULT_HOUR)
        set(value) = preferences.edit().putInt(KEY_HOUR, value).apply()

    var minute: Int
        get() = preferences.getInt(KEY_MINUTE, DEFAULT_MINUTE)
        set(value) = preferences.edit().putInt(KEY_MINUTE, value).apply()

    fun clear() {
        preferences.edit().clear().apply()
    }

    fun time(): LocalTime = LocalTime.of(hour, minute)

    private companion object {
        const val PREFS_NAME = "scheduled_reminders"
        const val KEY_ENABLED = "enabled"
        const val KEY_HOUR = "hour"
        const val KEY_MINUTE = "minute"
        const val DEFAULT_HOUR = 18
        const val DEFAULT_MINUTE = 0
    }
}
