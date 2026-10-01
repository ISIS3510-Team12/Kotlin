package com.team12kotlin.juggle.ui.profile.settings.location

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Arrow_back
import com.composables.icons.materialsymbols.outlined.Chevron_right
import com.composables.icons.materialsymbols.outlinedfilled.Location_on
import com.team12kotlin.juggle.ui.components.PillButton
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@Composable
fun LocationRemindersScreen(
    modifier: Modifier = Modifier,
    viewModel: LocationRemindersViewModel = viewModel(),
    goBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LocationRemindersContent(
        modifier = modifier,
        uiState = uiState,
        goBack = goBack,
        onChooseOnMapClick = viewModel::onChooseOnMapClick,
        onRadiusClick = viewModel::onRadiusClick,
        onRadiusDialogDismiss = viewModel::onRadiusDialogDismiss,
        onRadiusSelected = viewModel::onRadiusSelected,
        onSaveLocation = {
            viewModel.onSaveLocation()
            goBack()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationRemindersContent(
    uiState: LocationRemindersUiState,
    modifier: Modifier = Modifier,
    goBack: () -> Unit = {},
    onChooseOnMapClick: () -> Unit = {},
    onRadiusClick: () -> Unit = {},
    onRadiusDialogDismiss: () -> Unit = {},
    onRadiusSelected: (Int) -> Unit = {},
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
            MapPreview(onClick = onChooseOnMapClick)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clickable(role = Role.Button, onClick = onRadiusClick)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Notify within", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = formatRadius(uiState.radiusMeters),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = MaterialSymbols.Outlined.Chevron_right,
                        contentDescription = null
                    )
                }
            }
            PillButton(text = "Save location", onClick = onSaveLocation)
        }
    }

    if (uiState.isRadiusDialogVisible) {
        RadiusDialog(
            selectedMeters = uiState.radiusMeters,
            onSelected = onRadiusSelected,
            onDismiss = onRadiusDialogDismiss
        )
    }
}

@Composable
private fun MapPreview(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = MaterialSymbols.OutlinedFilled.Location_on,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Tap to choose on map",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RadiusDialog(
    selectedMeters: Int,
    onSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notify within") },
        text = {
            Column {
                NOTIFY_RADIUS_OPTIONS_METERS.forEach { meters ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.RadioButton) { onSelected(meters) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = meters == selectedMeters, onClick = null)
                        Box(modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp)) {
                            Text(formatRadius(meters))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun LocationRemindersScreenPreview() {
    JuggleTheme {
        LocationRemindersContent(uiState = LocationRemindersUiState())
    }
}
