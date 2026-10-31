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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.team12kotlin.juggle.reminders.LocationReminderSync
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team12kotlin.juggle.ui.components.BackTopBar
import com.team12kotlin.juggle.ui.telemetry.ScreenName
import com.team12kotlin.juggle.ui.telemetry.TrackScreenLoad
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Chevron_right
import com.composables.icons.materialsymbols.outlined.Edit
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
        onShakeChange = viewModel::onShakeChange,
        onDailyRemindersChange = viewModel::onDailyRemindersChange,
        onDailyReminderTimeChange = viewModel::onDailyReminderTimeChange,
        onLocationRemindersClick = onLocationRemindersClick,
        onSignOutClick = {
            viewModel.onSignOutRequested(onSignedOut)
        }
    )

    uiState.pendingSignOutChanges?.let { pendingChanges ->
        SignOutConfirmationDialog(
            pendingChanges = pendingChanges,
            onDismiss = viewModel::onSignOutDismissed,
            onConfirm = {
                LocationReminderSync.onSignedOut(context)
                viewModel.onSignOutConfirmed(onSignedOut)
            }
        )
    }
}

@Composable
private fun SignOutConfirmationDialog(
    pendingChanges: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val changes = if (pendingChanges == 1) "change is" else "changes are"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sign out?") },
        text = {
            Text(
                "$pendingChanges $changes still waiting to upload and will be discarded if you sign out."
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Stay signed in")
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Sign out")
            }
        }
    )
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    modifier: Modifier = Modifier,
    goBack: () -> Unit = {},
    onThemeClick: () -> Unit = {},
    onSoundAndVibrationChange: (Boolean) -> Unit = {},
    onShakeChange: (Boolean) -> Unit = {},
    onDailyRemindersChange: (Boolean) -> Unit = {},
    onDailyReminderTimeChange: (Int, Int) -> Unit = {_,_ -> },
    onLocationRemindersClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BackTopBar(title = "Settings", onBack = goBack)
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
                        title = "Shake detector enabled",
                        supportingText = "Shake phone to reset fields on forms",
                        onClick = { onShakeChange(!uiState.shakeEnabled) }
                    ) {
                        Switch(
                            checked = uiState.shakeEnabled,
                            onCheckedChange = onShakeChange
                        )
                    }
                    SettingsItem(
                        title = "Location reminders",
                        supportingText = "Get notified near a saved place",
                        onClick = onLocationRemindersClick
                    ) {
                        ChevronIcon()
                    }
                    DailyReminderSettingsItem(
                        title = "Daily reminders",
                        supportingText = "Get notified at a specific time",
                        onClick = { onDailyRemindersChange(!uiState.dailyRemindersEnabled) },
                        showDetail = uiState.dailyRemindersEnabled,
                        hour = uiState.dailyReminderHour,
                        minute = uiState.dailyReminderMinute,
                        onDailyRemindersTimeChange = onDailyReminderTimeChange
                    ) {
                        Switch(
                            checked = uiState.dailyRemindersEnabled,
                            onCheckedChange = onDailyRemindersChange
                        )
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
    trailing: @Composable () -> Unit,
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
private fun DailyReminderSettingsItem(
    title: String,
    supportingText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDetail: Boolean = true,
    onDailyRemindersTimeChange: (Int, Int) -> Unit = { _, _->},
    hour: Int = 18,
    minute: Int = 0,
    trailing: @Composable () -> Unit,
) {
    SettingsItem(
        title = title,
        supportingText = supportingText,
        onClick = onClick,
        modifier = modifier,
        trailing = trailing
    )
    if (showDetail) {
        var showDialog by remember { mutableStateOf(false) }
        val timePickerState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = true
        )
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                thickness = 1.dp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = { showDialog = true })
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = "Every day at ${formatTime(hour, minute)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = { showDialog = true }) {
                    Icon(
                        imageVector = MaterialSymbols.Outlined.Edit,
                        contentDescription = "Pick a time",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Select time") },
                text = { TimePicker(state = timePickerState) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDailyRemindersTimeChange(timePickerState.hour, timePickerState.minute)
                            showDialog = false
                        }
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
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

@Preview(showBackground = true)
@Composable
private fun SettingsItemPreview() {
    JuggleTheme {
        SettingsItem(title = "Item", supportingText = "Supporting text", onClick = {})
        {
            ChevronIcon()
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)