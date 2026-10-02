package com.team12kotlin.juggle.reminders

import android.content.Context
import android.util.Log
import com.team12kotlin.juggle.data.Dependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "LocationReminder"

/**
 * Keeps the on-device geofence consistent with the place saved in the back. Geofences are lost on reboot,
 * app updates and sign-out, so this runs on start-up, sign-in, boot and update.
 */
object LocationReminderSync {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Registers the geofence for the saved place, or clears it when the user has none. */
    fun sync(context: Context) {
        val appContext = context.applicationContext
        scope.launch { syncNow(appContext) }
    }

    suspend fun syncNow(context: Context) {
        if (Dependencies.authRepository.currentUser == null) return
        val result = runCatching { Dependencies.locationRepository.getLocation() }
        if (result.isFailure) {
            // A failed read says nothing about the saved place, so keep whatever is registered.
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

    /** Drops the geofence and the "already reminded today" mark so the next account starts clean. */
    fun onSignedOut(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            GeofenceManager.clear(appContext)
            LocationReminderNotifier.reset(appContext)
        }
    }
}
