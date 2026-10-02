package com.team12kotlin.juggle.ui.profile.settings.location

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Arrow_back
import com.composables.icons.materialsymbols.outlined.My_location
import com.team12kotlin.juggle.reminders.GeofenceManager
import com.team12kotlin.juggle.reminders.rememberReminderPermissionsFlow
import com.team12kotlin.juggle.ui.components.PillButton
import com.team12kotlin.juggle.ui.dto.UserLocation
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@Composable
fun LocationRemindersScreen(
    modifier: Modifier = Modifier,
    viewModel: LocationRemindersViewModel = viewModel(),
    goBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val currentLocation = rememberCurrentLocationRequest(
        onLocation = viewModel::onLocationPicked,
        onError = viewModel::onLocationError
    )

    val context = LocalContext.current
    var permissionsComplete by remember { mutableStateOf(true) }
    val saveAfterPermissions = rememberReminderPermissionsFlow { allGranted ->
        permissionsComplete = allGranted
        viewModel.onSaveLocation()
    }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            // Registered with what was just saved; without location permission it's skipped and retried later.
            val latitude = uiState.latitude
            val longitude = uiState.longitude
            val radius = uiState.radiusMeters
            if (latitude != null && longitude != null && radius != null) {
                GeofenceManager.register(context, UserLocation(latitude, longitude, radius))
            }
            if (!permissionsComplete) {
                Toast.makeText(
                    context,
                    "Saved. Reminders need notifications and “Allow all the time” location to work in the background.",
                    Toast.LENGTH_LONG
                ).show()
            }
            goBack()
        }
    }

    LocationRemindersContent(
        modifier = modifier,
        uiState = uiState,
        goBack = goBack,
        onLocationPicked = viewModel::onLocationPicked,
        onUseCurrentLocation = { currentLocation.request() },
        onRadiusChange = viewModel::onRadiusChange,
        onSaveLocation = saveAfterPermissions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationRemindersContent(
    uiState: LocationRemindersUiState,
    modifier: Modifier = Modifier,
    goBack: () -> Unit = {},
    onLocationPicked: (Double, Double) -> Unit = { _, _ -> },
    onUseCurrentLocation: () -> Unit = {},
    onRadiusChange: (String) -> Unit = {},
    onSaveLocation: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Location reminders") },
                navigationIcon = {
                    IconButton(onClick = goBack) {
                        Icon(
                            imageVector = MaterialSymbols.Outlined.Arrow_back,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pick a place where you want to be reminded about your pending tasks. We’ll notify you when you’re nearby.",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (LocalInspectionMode.current) {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    content = {}
                )
            } else {
                LocationMap(
                    latitude = uiState.latitude,
                    longitude = uiState.longitude,
                    radiusMeters = uiState.radiusMeters,
                    onLocationPicked = onLocationPicked
                )
            }
            Text(
                text = if (uiState.hasLocation) "Tap the map to move the place." else "Tap the map to choose a place.",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(onClick = onUseCurrentLocation, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = MaterialSymbols.Outlined.My_location,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(text = "Use my current location", modifier = Modifier.padding(start = 8.dp))
            }
            OutlinedTextField(
                value = uiState.radiusText,
                onValueChange = onRadiusChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notify within (meters)") },
                suffix = { Text("m") },
                supportingText = { Text(uiState.radiusError ?: "Maximum 1000 m (1 km)") },
                isError = uiState.radiusError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            PillButton(text = "Save location", onClick = onSaveLocation, enabled = uiState.canSave)
        }
    }

}

@Preview(showBackground = true)
@Composable
private fun LocationRemindersScreenPreview() {
    JuggleTheme {
        LocationRemindersContent(uiState = LocationRemindersUiState())
    }
}
