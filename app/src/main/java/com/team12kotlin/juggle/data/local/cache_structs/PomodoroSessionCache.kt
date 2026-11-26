package com.team12kotlin.juggle.data.local.cache_structs

import android.util.LruCache

class PomodoroSessionCache {
    private val cache = LruCache<String, Int>(4)

    fun getCompletedSessions(): Int = cache.get(KEY_COMPLETED) ?: 0
    fun putCompletedSessions(value: Int) { cache.put(KEY_COMPLETED, value) }

    private companion object {
        const val KEY_COMPLETED = "completed_sessions"
    }
}