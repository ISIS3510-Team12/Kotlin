package com.team12kotlin.juggle.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) {
            Log.d("LocationReminder", "geofence error: ${event.errorCode}")
            return
        }
        Log.d("LocationReminder", "geofence transition: ${event.geofenceTransition}")
        if (event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER) return

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                LocationReminderNotifier.remindIfNeeded(context.applicationContext)
            } finally {
                pending.finish()
            }
        }
    }
}
