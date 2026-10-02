package com.team12kotlin.juggle.ui.profile.settings.location

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CurrentLocationRequest(val request: () -> Unit)

/** Asks for location permission when needed, then reports the phone's current position. */
@Composable
fun rememberCurrentLocationRequest(
    onLocation: (latitude: Double, longitude: Double) -> Unit,
    onError: (String) -> Unit
): CurrentLocationRequest {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentOnLocation = rememberUpdatedState(onLocation)
    val currentOnError = rememberUpdatedState(onError)

    fun fetch() {
        scope.launch {
            val client = LocationServices.getFusedLocationProviderClient(context)
            // A fresh fix can time out indoors; the last known one is good enough to place a pin.
            val location = runCatching {
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            }.getOrNull() ?: runCatching { client.lastLocation.await() }.getOrNull()
            if (location != null) {
                currentOnLocation.value(location.latitude, location.longitude)
            } else {
                currentOnError.value("Couldn't get your current location. Make sure location is turned on.")
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) {
            fetch()
        } else {
            currentOnError.value("Location permission is needed to use your current location. You can still pick a place on the map.")
        }
    }

    return CurrentLocationRequest {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) {
            fetch()
        } else {
            launcher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }
}
