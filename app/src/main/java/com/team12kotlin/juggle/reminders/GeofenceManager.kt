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

/**
 * Registers and removes the geofence for the saved location.
 *
 * Geofencing implementation based on:
 * https://medium.com/@thammy202/implementing-geofencing-in-android-using-kotlin-399c560c2363
 */
object GeofenceManager {

    fun canRegister(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /** Registers the geofence. Returns true if it was added. */
    @SuppressLint("MissingPermission")
    suspend fun register(context: Context, location: UserLocation): Boolean {
        if (!canRegister(context)) return false
        val geofence = Geofence.Builder()
            .setRequestId(REQUEST_ID)
            .setCircularRegion(location.latitude, location.longitude, location.notifyWithin.toFloat())
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            // Listen for both transitions.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
        // Trigger on enter if the device is already inside.
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

    // The PendingIntent must be mutable to receive geofence events.
    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, GeofenceReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )
}
