package com.team12kotlin.juggle.ui.tasks.create

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Calendar_month
import com.composables.icons.materialsymbols.outlined.Schedule
import com.composables.icons.materialsymbols.outlined.Search
import com.composables.icons.materialsymbols.outlined.Upload
import com.team12kotlin.juggle.ui.components.TextMonogram
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.io.File

// Reusable building blocks for task forms (Create Task and, later, Edit Task).

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DropdownField(
    value: String?,
    label: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // A text field that expands into a menu of options.
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DatePickerField(
    value: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // A text field whose trailing icon opens a Material date picker dialog.
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        modifier = modifier.fillMaxWidth(),
        label = { Text("Date") },
        supportingText = { Text("MM/DD/YYYY") },
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(
                    imageVector = MaterialSymbols.Outlined.Calendar_month,
                    contentDescription = "Pick a date"
                )
            }
        }
    )

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onDateSelected(formatSelectedDate(it))
                        }
                        showDialog = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TimePickerField(
    value: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // A text field whose trailing icon opens a Material time picker dialog.
    var showDialog by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(is24Hour = true)

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        modifier = modifier.fillMaxWidth(),
        label = { Text("Time") },
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(
                    imageVector = MaterialSymbols.Outlined.Schedule,
                    contentDescription = "Pick a time"
                )
            }
        }
    )

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeSelected(formatTime(timePickerState.hour, timePickerState.minute))
                        showDialog = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
internal fun AssignedMemberChip(
    member: AssignableMember,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // A selectable member, with a monogram avatar over a name pill.
    val containerColor = if (member.selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (member.selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TextMonogram(
            text = member.initial,
            containerColor = containerColor,
            contentColor = contentColor
        )
        Text(
            text = member.name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // A label with a switch (Is priority / Needs help).
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
internal fun RelatedTaskItem(
    relatedTask: RelatedTask,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    // A related task shown as a list item with a monogram and a trailing checkbox.
    ListItem(
        modifier = modifier
            .clickable { onToggle() }
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = {
            Text(
                text = relatedTask.task.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = { Text(relatedTask.dueLabel) },
        leadingContent = {
            TextMonogram(
                text = relatedTask.task.assignees.firstOrNull()?.firstName?.take(1)?.uppercase()
                    ?: relatedTask.task.title.take(1).uppercase()
            )
        },
        trailingContent = {
            Checkbox(checked = relatedTask.selected, onCheckedChange = { onToggle() })
        }
    )
}

private val RELATED_TASKS_HEIGHT = 216.dp

@Composable
internal fun RelatedTasksPicker(
    query: String,
    onQueryChange: (String) -> Unit,
    tasks: List<RelatedTask>,
    onToggle: (RelatedTask) -> Unit,
    modifier: Modifier = Modifier
) {
    // A searchable list of related tasks that shows three at a time and scrolls.
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(100.dp),
            placeholder = { Text("Search for a task...") },
            leadingIcon = {
                Icon(
                    imageVector = MaterialSymbols.Outlined.Search,
                    contentDescription = "Search"
                )
            }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(RELATED_TASKS_HEIGHT)
                .verticalScroll(rememberScrollState())
        ) {
            tasks.forEach { related ->
                RelatedTaskItem(
                    relatedTask = related,
                    onToggle = { onToggle(related) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
internal fun EvidencesField(
    imageBytes: ByteArray?,
    onPhotoTaken: (bytes: ByteArray, mimeType: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Tapping opens the camera; once a photo exists it is shown as a preview and
    // tapping again retakes it.
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingUri
        if (success && uri != null) {
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                    if (bytes != null) bytes to mimeType else null
                }
                result?.let { (bytes, mimeType) -> onPhotoTaken(bytes, mimeType) }
            }
        }
    }

    fun takePhoto() {
        val uri = createEvidenceUri(context)
        pendingUri = uri
        cameraLauncher.launch(uri)
    }

    val preview = remember(imageBytes) {
        imageBytes?.let { bytes ->
            runCatching {
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            }.getOrNull()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "EVIDENCES",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clickable { takePhoto() },
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (preview != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        bitmap = preview,
                        contentDescription = "Evidence",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = "Tap to retake",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(8.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Outlined.Upload,
                        contentDescription = "Take a photo"
                    )
                    Text(
                        text = "Take a photo related to the task",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
internal fun EvidenceView(
    imageBytes: ByteArray?,
    modifier: Modifier = Modifier
) {
    // Read-only evidence: shown as a preview and tappable to expand it.
    var expanded by remember { mutableStateOf(false) }

    val preview = remember(imageBytes) {
        imageBytes?.let { bytes ->
            runCatching {
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            }.getOrNull()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "EVIDENCES",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val clickableModifier = if (preview != null) {
            Modifier.clickable { expanded = true }
        } else {
            Modifier
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .then(clickableModifier),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (preview != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        bitmap = preview,
                        contentDescription = "Evidence",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = "Tap to expand",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(8.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No photo attached",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    if (expanded && preview != null) {
        Dialog(onDismissRequest = { expanded = false }) {
            Image(
                bitmap = preview,
                contentDescription = "Evidence",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

private fun createEvidenceUri(context: Context): Uri {
    val directory = File(context.filesDir, "evidences").apply { mkdirs() }
    val file = File.createTempFile("evidence_", ".jpg", directory)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private fun formatSelectedDate(millis: Long): String {
    // DatePicker returns the selection at UTC midnight, so read it back in UTC.
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return formatter.format(Date(millis))
}

private fun formatTime(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)
