package com.team12kotlin.juggle.ui.profile.settings.location

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Arrow_back
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

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) goBack()
    }

    LocationRemindersContent(
        modifier = modifier,
        uiState = uiState,
        goBack = goBack,
        onChooseOnMapClick = viewModel::onChooseOnMapClick,
        onRadiusChange = viewModel::onRadiusChange,
        onSaveLocation = viewModel::onSaveLocation
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationRemindersContent(
    uiState: LocationRemindersUiState,
    modifier: Modifier = Modifier,
    goBack: () -> Unit = {},
    onChooseOnMapClick: () -> Unit = {},
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
            MapPreview(
                latitude = uiState.latitude,
                longitude = uiState.longitude,
                onClick = onChooseOnMapClick
            )
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

@Composable
private fun MapPreview(
    latitude: Double?,
    longitude: Double?,
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
                text = if (latitude != null && longitude != null) "%.5f, %.5f".format(latitude, longitude) else "Tap to choose on map",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
