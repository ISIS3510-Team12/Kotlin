package com.team12kotlin.juggle.data.local

import android.content.Context
import android.content.SharedPreferences

object UserCache {

    private const val PREFS = "cached_user"
    private const val KEY_FIRST_NAME = "firstName"
    private const val KEY_DISPLAY_NAME = "displayName"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    fun save(firstName: String, displayName: String) {
        if (firstName.isBlank() && displayName.isBlank()) return
        prefs?.edit()
            ?.putString(KEY_FIRST_NAME, firstName)
            ?.putString(KEY_DISPLAY_NAME, displayName)
            ?.apply()
    }

    fun firstName(): String = prefs?.getString(KEY_FIRST_NAME, "").orEmpty()

    fun displayName(): String = prefs?.getString(KEY_DISPLAY_NAME, "").orEmpty()

    fun clear() {
        prefs?.edit()?.clear()?.apply()
    }
}
