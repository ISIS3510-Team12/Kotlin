package com.team12kotlin.juggle.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LastSync {

    private const val PREFS = "last_sync"
    private const val KEY_TIMESTAMP = "timestamp"

    private var prefs: SharedPreferences? = null

    private val _timestamp = MutableStateFlow<Long?>(null)
    val timestamp: StateFlow<Long?> = _timestamp.asStateFlow()

    fun init(context: Context) {
        val stored = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = stored
        _timestamp.value = stored.getLong(KEY_TIMESTAMP, 0L).takeIf { it > 0L }
    }

    fun mark(at: Long) {
        _timestamp.value = at
        prefs?.edit()?.putLong(KEY_TIMESTAMP, at)?.apply()
    }

    fun clear() {
        _timestamp.value = null
        prefs?.edit()?.remove(KEY_TIMESTAMP)?.apply()
    }
}
