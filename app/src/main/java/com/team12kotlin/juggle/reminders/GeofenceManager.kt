package com.team12kotlin.juggle.reminders

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.team12kotlin.juggle.ui.dto.UserLocation
import kotlinx.coroutines.tasks.await

private const val REQUEST_ID = "saved_location"

/** Keeps a single geofence around the user's saved place; entering it triggers [GeofenceReceiver]. */
object GeofenceManager {

    fun canRegister(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /** Adds (or replaces) the geofence. Returns false when it couldn't be registered. */
    @SuppressLint("MissingPermission")
    suspend fun register(context: Context, location: UserLocation): Boolean {
        if (!canRegister(context)) return false
        val geofence = Geofence.Builder()
            .setRequestId(REQUEST_ID)
            .setCircularRegion(location.latitude, location.longitude, location.notifyWithin.toFloat())
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            // EXIT is registered only so Play Services tracks leaving the area; without it a later re-entry is
            // dropped as "already in state". The receiver reacts to ENTER alone.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
        // Being already inside when it's registered counts as entering.
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
        return runCatching {
            LocationServices.getGeofencingClient(context).addGeofences(request, pendingIntent(context)).await()
        }.isSuccess
    }

    suspend fun clear(context: Context) {
        runCatching {
            LocationServices.getGeofencingClient(context).removeGeofences(pendingIntent(context)).await()
        }
    }

    // Geofence events carry extras, so the PendingIntent has to be mutable.
    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, GeofenceReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )
}
