package com.team12kotlin.juggle

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.team12kotlin.juggle.reminders.LocationReminderSync
import com.team12kotlin.juggle.ui.theme.JuggleTheme

class MainActivity : ComponentActivity() {

    private val localNetworkPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestLocalNetworkAccessIfNeeded()
        LocationReminderSync.sync(this)
        setContent {
            JuggleTheme {
                AppRoot()
            }
        }
    }

    /**
     * Android 17 enforces local network protections for
     * connections to local/private addresses (for example, the emulator host 10.0.2.2)
     * which are dropped unless ACCESS_LOCAL_NETWORK is granted. I added this because
     * it's only required for local development against the Firebase Auth emulator and backend.
     */
    private fun requestLocalNetworkAccessIfNeeded() {
        if (!BuildConfig.USE_FIREBASE_EMULATOR) return

        val permission = "android.permission.ACCESS_LOCAL_NETWORK"
        val granted = ContextCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) {
            localNetworkPermissionLauncher.launch(permission)
        }
    }
}
