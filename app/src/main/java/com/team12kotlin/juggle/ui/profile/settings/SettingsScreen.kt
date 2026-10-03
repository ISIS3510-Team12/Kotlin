package com.team12kotlin.juggle.ui.profile.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.team12kotlin.juggle.reminders.LocationReminderSync
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team12kotlin.juggle.ui.telemetry.ScreenName
import com.team12kotlin.juggle.ui.telemetry.TrackScreenLoad
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Arrow_back
import com.composables.icons.materialsymbols.outlined.Chevron_right
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
    goBack: () -> Unit = {},
    onLocationRemindersClick: () -> Unit = {},
    onSignedOut: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrackScreenLoad(ScreenName.Settings)
    val context = LocalContext.current

    SettingsContent(
        modifier = modifier,
        uiState = uiState,
        goBack = goBack,
        onThemeClick = viewModel::onThemeClick,
        onSoundAndVibrationChange = viewModel::onSoundAndVibrationChange,
        onLocationRemindersClick = onLocationRemindersClick,
        onSignOutClick = {
            LocationReminderSync.onSignedOut(context)
            viewModel.onSignOut(onSignedOut)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    modifier: Modifier = Modifier,
    goBack: () -> Unit = {},
    onThemeClick: () -> Unit = {},
    onSoundAndVibrationChange: (Boolean) -> Unit = {},
    onLocationRemindersClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
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
                .padding(start = 24.dp, top = 10.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingsItem(
                        title = "Theme",
                        supportingText = "Use system appearance",
                        onClick = onThemeClick
                    ) {
                        ChevronIcon()
                    }
                    SettingsItem(
                        title = "Sound & vibration",
                        supportingText = "Control app sounds and haptics",
                        onClick = { onSoundAndVibrationChange(!uiState.soundAndVibrationEnabled) }
                    ) {
                        Switch(
                            checked = uiState.soundAndVibrationEnabled,
                            onCheckedChange = onSoundAndVibrationChange
                        )
                    }
                    SettingsItem(
                        title = "Location reminders",
                        supportingText = "Get notified near a saved place",
                        onClick = onLocationRemindersClick
                    ) {
                        ChevronIcon()
                    }
                }
            }
            Button(
                onClick = onSignOutClick,
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(text = "Sign out")
            }
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    supportingText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        trailing()
    }
}

@Composable
private fun ChevronIcon() {
    Icon(
        imageVector = MaterialSymbols.Outlined.Chevron_right,
        contentDescription = null
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    JuggleTheme {
        SettingsContent(uiState = SettingsUiState())
    }
}
