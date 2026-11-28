package com.team12kotlin.juggle.ui.tools.wheel

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Delete
import com.composables.icons.materialsymbols.outlined.Edit
import com.composables.icons.materialsymbols.outlined.Replay
import com.team12kotlin.juggle.ui.components.BackTopBar
import com.team12kotlin.juggle.ui.components.LoadingContent
import com.team12kotlin.juggle.ui.dto.MAX_WHEEL_OPTIONS
import com.team12kotlin.juggle.ui.dto.MIN_WHEEL_OPTIONS
import com.team12kotlin.juggle.ui.dto.WheelOption
import com.team12kotlin.juggle.ui.telemetry.ScreenName
import com.team12kotlin.juggle.ui.telemetry.TrackScreenLoad
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import com.team12kotlin.juggle.ui.utils.WheelFieldLimits

private const val SPIN_TURNS = 5
private const val SPIN_MILLIS = 4500
private const val FULL_TURN = (2 * Math.PI).toFloat()
private val EaseOutCubic = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1f)

@Composable
fun WheelEditorScreen(
    wheelId: Int,
    modifier: Modifier = Modifier,
    viewModel: WheelEditorViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrackScreenLoad(ScreenName.WheelEditor, uiState.isLoading)

    LaunchedEffect(wheelId) { viewModel.load(wheelId) }
    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) onBackClick()
    }

    // The wheel keeps the angle where it stopped, so the next spin starts from there.
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(uiState.spinTarget) {
        val target = uiState.spinTarget ?: return@LaunchedEffect
        val segment = FULL_TURN / target.optionCount
        val stopAt = -(target.index * segment + segment / 2)
        val remaining = ((stopAt - rotation.value % FULL_TURN) % FULL_TURN + FULL_TURN) % FULL_TURN
        rotation.animateTo(
            targetValue = rotation.value + SPIN_TURNS * FULL_TURN + remaining,
            animationSpec = tween(durationMillis = SPIN_MILLIS, easing = EaseOutCubic)
        )
        viewModel.onSpinFinished()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorShown()
        }
    }

    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showRenameDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var editing by remember { mutableStateOf<WheelOption?>(null) }

    if (showAddDialog) {
        WheelTextDialog(
            title = "Add option",
            label = "Option",
            initialText = "",
            maxLength = WheelFieldLimits.OPTION_MAX_LENGTH,
            confirmLabel = "Add",
            onConfirm = {
                viewModel.onAddOption(it)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
    editing?.let { option ->
        WheelTextDialog(
            title = "Edit option",
            label = "Option",
            initialText = option.text,
            maxLength = WheelFieldLimits.OPTION_MAX_LENGTH,
            confirmLabel = "Save",
            onConfirm = {
                viewModel.onEditOption(option, it)
                editing = null
            },
            onDismiss = { editing = null }
        )
    }
    if (showRenameDialog) {
        WheelTextDialog(
            title = "Rename wheel",
            label = "Wheel name",
            initialText = uiState.name,
            maxLength = WheelFieldLimits.NAME_MAX_LENGTH,
            confirmLabel = "Save",
            onConfirm = {
                viewModel.onRename(it)
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false }
        )
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete wheel?") },
            text = { Text("\"${uiState.name}\" and its options will be removed from this device.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.onDeleteWheel()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    WheelEditorContent(
        uiState = uiState,
        rotation = rotation.value,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
        onBackClick = onBackClick,
        onSpin = viewModel::onSpin,
        onAddOptionClick = { showAddDialog = true },
        onEditOption = { editing = it },
        onDeleteOption = viewModel::onDeleteOption,
        onRenameClick = { showRenameDialog = true },
        onDeleteWheelClick = { showDeleteDialog = true },
        onSpinAgain = viewModel::onSpinAgain,
        onRemoveResult = viewModel::onRemoveResult,
        onResultDismissed = viewModel::onResultDismissed
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WheelEditorContent(
    uiState: WheelEditorUiState,
    rotation: Float,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSpin: () -> Unit = {},
    onAddOptionClick: () -> Unit = {},
    onEditOption: (WheelOption) -> Unit = {},
    onDeleteOption: (WheelOption) -> Unit = {},
    onRenameClick: () -> Unit = {},
    onDeleteWheelClick: () -> Unit = {},
    onSpinAgain: () -> Unit = {},
    onRemoveResult: () -> Unit = {},
    onResultDismissed: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 104.dp))
        },
        topBar = { BackTopBar(title = uiState.name.ifEmpty { "Wheel" }, onBack = onBackClick) }
    ) { innerPadding ->
        LoadingContent(isLoading = uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "${uiState.options.size} of $MAX_WHEEL_OPTIONS options",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (uiState.options.isEmpty()) {
                    Text(
                        text = "Add options to build the wheel",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 120.dp)
                    )
                } else {
                    DecisionWheel(
                        options = uiState.options.map { it.text },
                        rotation = rotation,
                        modifier = Modifier.semantics {
                            contentDescription = "Decision wheel with ${uiState.options.size} options"
                        }
                    )
                }

                Button(onClick = onSpin, enabled = uiState.canSpin) {
                    Icon(imageVector = MaterialSymbols.Outlined.Replay, contentDescription = null)
                    Text(text = "Spin the wheel", modifier = Modifier.padding(start = 8.dp))
                }
                if (uiState.options.size < MIN_WHEEL_OPTIONS) {
                    Text(
                        text = "Add at least two options to spin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "Options", style = MaterialTheme.typography.headlineMedium)
                    uiState.options.forEach { option ->
                        OptionRow(
                            option = option,
                            enabled = !uiState.isSpinning,
                            onEdit = { onEditOption(option) },
                            onDelete = { onDeleteOption(option) }
                        )
                    }
                    AddOptionButton(
                        enabled = uiState.canAddOption && !uiState.isSpinning,
                        onClick = onAddOptionClick
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onRenameClick, enabled = !uiState.isSpinning) {
                        Icon(imageVector = MaterialSymbols.Outlined.Edit, contentDescription = null)
                        Text(text = "Rename", modifier = Modifier.padding(start = 4.dp))
                    }
                    OutlinedButton(onClick = onDeleteWheelClick, enabled = !uiState.isSpinning) {
                        Icon(
                            imageVector = MaterialSymbols.Outlined.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Delete wheel",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(136.dp))
            }
        }
    }

    uiState.result?.let { result ->
        ModalBottomSheet(onDismissRequest = onResultDismissed) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "The wheel chose",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = result.text,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Button(onClick = onSpinAgain, modifier = Modifier.fillMaxWidth()) {
                    Icon(imageVector = MaterialSymbols.Outlined.Replay, contentDescription = null)
                    Text(text = "Spin again", modifier = Modifier.padding(start = 8.dp))
                }
                OutlinedButton(onClick = onRemoveResult, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Remove from the wheel")
                }
            }
        }
    }
}

@Composable
private fun OptionRow(
    option: WheelOption,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = option.text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = onEdit, enabled = enabled) {
                Icon(imageVector = MaterialSymbols.Outlined.Edit, contentDescription = "Edit option")
            }
            IconButton(onClick = onDelete, enabled = enabled) {
                Icon(imageVector = MaterialSymbols.Outlined.Delete, contentDescription = "Delete option")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WheelEditorScreenPreview() {
    JuggleTheme {
        WheelEditorContent(
            uiState = WheelEditorUiState(
                name = "What should we eat?",
                options = listOf("Pizza", "Sushi", "Tacos", "Burgers").mapIndexed { i, text -> WheelOption(i, text) },
                isLoading = false
            ),
            rotation = 0f,
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
