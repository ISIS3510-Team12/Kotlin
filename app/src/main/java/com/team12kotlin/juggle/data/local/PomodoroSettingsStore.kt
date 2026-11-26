package com.team12kotlin.juggle.data.local

import android.content.Context

class PomodoroSettingsStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var focusMinutes: Int
        get() = prefs.getInt(KEY_FOCUS, 25)
        set(value) = prefs.edit().putInt(KEY_FOCUS, value).apply()

    var breakMinutes: Int
        get() = prefs.getInt(KEY_BREAK, 5)
        set(value) = prefs.edit().putInt(KEY_BREAK, value).apply()

    private companion object {
        const val PREFS_NAME = "pomodoro_prefs"
        const val KEY_FOCUS = "focus_minutes"
        const val KEY_BREAK = "break_minutes"
    }
}