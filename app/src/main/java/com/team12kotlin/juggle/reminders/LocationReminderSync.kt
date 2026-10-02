package com.team12kotlin.juggle.reminders

import android.content.Context
import android.util.Log
import com.team12kotlin.juggle.data.Dependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "LocationReminder"

/** Keeps the geofence in sync with the saved location. */
object LocationReminderSync {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Registers or clears the geofence. */
    fun sync(context: Context) {
        val appContext = context.applicationContext
        scope.launch { syncNow(appContext) }
    }

    suspend fun syncNow(context: Context) {
        if (Dependencies.authRepository.currentUser == null) return
        val result = runCatching { Dependencies.locationRepository.getLocation() }
        if (result.isFailure) {
            // Keep the current geofence if the request fails.
            Log.d(TAG, "sync skipped: couldn't read the saved location")
            return
        }
        val location = result.getOrNull()
        if (location == null) {
            GeofenceManager.clear(context)
        } else {
            Log.d(TAG, "sync: geofence registered=${GeofenceManager.register(context, location)}")
        }
    }

    /** Clears the geofence and reminder data when the user signs out. */
    fun onSignedOut(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            GeofenceManager.clear(appContext)
            LocationReminderNotifier.reset(appContext)
        }
    }
}
