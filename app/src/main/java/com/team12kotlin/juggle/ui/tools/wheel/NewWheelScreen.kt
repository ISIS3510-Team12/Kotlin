package com.team12kotlin.juggle.ui.tools.wheel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Check
import com.composables.icons.materialsymbols.outlined.Close
import com.composables.icons.materialsymbols.outlined.Delete
import com.team12kotlin.juggle.ui.components.BackTopBar
import com.team12kotlin.juggle.ui.components.ErrorCard
import com.team12kotlin.juggle.ui.components.ShakeToReset
import com.team12kotlin.juggle.ui.components.SupportingTextWithCounter
import com.team12kotlin.juggle.ui.dto.MAX_WHEEL_OPTIONS
import com.team12kotlin.juggle.ui.telemetry.ScreenName
import com.team12kotlin.juggle.ui.telemetry.TrackScreenLoad
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import com.team12kotlin.juggle.ui.utils.WheelFieldLimits

@Composable
fun NewWheelScreen(
    modifier: Modifier = Modifier,
    viewModel: NewWheelViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onWheelCreated: () -> Unit = onBackClick
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrackScreenLoad(ScreenName.NewWheel)

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onWheelCreated()
    }

    ShakeToReset(enabled = !uiState.isSaving, onReset = viewModel::onResetForm)

    var showOptionDialog by rememberSaveable { mutableStateOf(false) }
    if (showOptionDialog) {
        WheelTextDialog(
            title = "Add option",
            label = "Option",
            initialText = "",
            maxLength = WheelFieldLimits.OPTION_MAX_LENGTH,
            confirmLabel = "Add",
            onConfirm = {
                viewModel.onAddOption(it)
                showOptionDialog = false
            },
            onDismiss = { showOptionDialog = false }
        )
    }

    NewWheelContent(
        uiState = uiState,
        modifier = modifier,
        onBackClick = onBackClick,
        onNameChange = viewModel::onNameChange,
        onAddOptionClick = { showOptionDialog = true },
        onRemoveOption = viewModel::onRemoveOption,
        onSave = viewModel::onSave
    )
}

@Composable
private fun NewWheelContent(
    uiState: NewWheelUiState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onAddOptionClick: () -> Unit = {},
    onRemoveOption: (Int) -> Unit = {},
    onSave: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = { BackTopBar(title = "New wheel", onBack = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text("Wheel name") },
                singleLine = true,
                supportingText = {
                    SupportingTextWithCounter(
                        text = "For example: What should we eat?",
                        length = uiState.name.length,
                        maxLength = WheelFieldLimits.NAME_MAX_LENGTH
                    )
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Options", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "${uiState.options.size} of $MAX_WHEEL_OPTIONS",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.options.forEachIndexed { index, option ->
                    OptionRow(text = option, onRemove = { onRemoveOption(index) })
                }
            }

            AddOptionButton(
                enabled = uiState.canAddOption,
                onClick = onAddOptionClick,
                modifier = Modifier.padding(horizontal = 18.dp)
            )

            uiState.errorMessage?.let { message ->
                ErrorCard(message = message, modifier = Modifier.padding(horizontal = 16.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onSave, enabled = uiState.canSave) {
                    Icon(imageVector = MaterialSymbols.Outlined.Check, contentDescription = "Check")
                    Text(text = "Save wheel", modifier = Modifier.padding(start = 4.dp))
                }
                OutlinedButton(onClick = onBackClick) {
                    Icon(imageVector = MaterialSymbols.Outlined.Delete, contentDescription = "Cancel")
                    Text(text = "Cancel", modifier = Modifier.padding(start = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(136.dp))
        }
    }
}

@Composable
private fun OptionRow(
    text: String,
    modifier: Modifier = Modifier,
    onRemove: () -> Unit = {}
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = MaterialSymbols.Outlined.Close,
                    contentDescription = "Remove option"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NewWheelScreenPreview() {
    JuggleTheme {
        NewWheelContent(
            uiState = NewWheelUiState(name = "What should we eat?", options = listOf("Pizza", "Sushi", "Tacos"))
        )
    }
}
