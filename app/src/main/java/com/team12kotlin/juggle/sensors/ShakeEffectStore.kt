package com.team12kotlin.juggle.sensors

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ShakeEffectStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _enabledFlow = MutableStateFlow(preferences.getBoolean(KEY_ENABLED, DEFAULT_ENABLED))
    val enabledFlow: StateFlow<Boolean> = _enabledFlow.asStateFlow()

    var enabled: Boolean
        get() = preferences.getBoolean(KEY_ENABLED, DEFAULT_ENABLED)
        set(value) {
            preferences.edit().putBoolean(KEY_ENABLED, value).apply()
            _enabledFlow.value = value
        }

    private companion object {
        const val PREFS_NAME = "shake_effect"
        const val KEY_ENABLED = "enabled"
        const val DEFAULT_ENABLED = true
    }
}