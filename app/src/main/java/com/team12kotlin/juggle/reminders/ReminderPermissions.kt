package com.team12kotlin.juggle.reminders

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

fun hasAllReminderPermissions(context: Context): Boolean = listOf(
    Manifest.permission.POST_NOTIFICATIONS,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_BACKGROUND_LOCATION
).all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

/**
 * Walks through what location reminders need, one system prompt at a time: notifications, location, and
 * "allow all the time" (explained first, since the system sends the user to Settings for it).
 * Returns a function that starts the flow; [onDone] runs at the end whether or not everything was granted,
 * so the saved place is kept even if the user declines.
 */
@Composable
fun rememberReminderPermissionsFlow(onDone: (allGranted: Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val currentOnDone by rememberUpdatedState(onDone)
    var showBackgroundRationale by remember { mutableStateOf(false) }

    fun granted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun finish() = currentOnDone(hasAllReminderPermissions(context))

    val backgroundLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        finish()
    }

    fun askBackground() {
        // Background location can only be asked once foreground location is there.
        if (granted(Manifest.permission.ACCESS_BACKGROUND_LOCATION) || !granted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            finish()
        } else {
            showBackgroundRationale = true
        }
    }

    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        askBackground()
    }

    fun askLocation() {
        if (granted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            askBackground()
        } else {
            locationLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        askLocation()
    }

    if (showBackgroundRationale) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Allow location all the time") },
            text = {
                Text(
                    "To remind you about your tasks when you get to your saved place, even if Juggle is closed, " +
                        "choose “Allow all the time” on the next screen."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showBackgroundRationale = false
                    backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBackgroundRationale = false
                    finish()
                }) { Text("Not now") }
            }
        )
    }

    return {
        if (granted(Manifest.permission.POST_NOTIFICATIONS)) {
            askLocation()
        } else {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
