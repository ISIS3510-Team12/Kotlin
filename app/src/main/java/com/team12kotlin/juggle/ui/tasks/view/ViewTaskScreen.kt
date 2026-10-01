package com.team12kotlin.juggle.ui.tasks.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Arrow_back
import com.composables.icons.materialsymbols.outlined.Calendar_month
import com.composables.icons.materialsymbols.outlined.Chat
import com.composables.icons.materialsymbols.outlined.Check
import com.composables.icons.materialsymbols.outlined.Close
import com.composables.icons.materialsymbols.outlined.Delete
import com.composables.icons.materialsymbols.outlined.Edit
import com.composables.icons.materialsymbols.outlined.Info
import com.composables.icons.materialsymbols.outlined.Star
import com.composables.icons.materialsymbols.outlined.Stars
import com.team12kotlin.juggle.ui.components.TextMonogram
import com.team12kotlin.juggle.ui.dto.RelatedTask
import com.team12kotlin.juggle.ui.dto.Reminder
import com.team12kotlin.juggle.ui.tasks.create.EvidenceView
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.utils.taskDue
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewTaskScreen(
    modifier: Modifier = Modifier,
    viewModel: ViewTaskViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onEditTask: (taskId: Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = uiState.isFabMenuExpanded) {
        viewModel.onFabMenuDismiss()
    }

    LifecycleResumeEffect(Unit) {
        viewModel.loadTask()
        onPauseOrDispose { }
    }

    Box(modifier = modifier) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("View task") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = MaterialSymbols.Outlined.Arrow_back,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            val task = uiState.task
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 26.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title + description
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status + priority chips
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AssistiveChip(
                        icon = MaterialSymbols.Outlined.Info,
                        label = task.status.label,
                        contentColor = MaterialTheme.colorScheme.outline
                    )
                    if (task.isPriority) {
                        AssistiveChip(
                            icon = MaterialSymbols.Outlined.Info,
                            label = "High priority",
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Scheduled card
                ScheduledCard(
                    deadline = task.deadline ?: "No deadline set",
                    reminder = task.reminders.firstOrNull(),
                    onReminderToggle = viewModel::onReminderToggle,
                    onEditClick = { onEditTask(task.id) }
                )

                // Assigned members
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ASSIGNED MEMBERS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                    TextButton(
                        onClick = { onEditTask(task.id) },
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("EDIT")
                    }
                }
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    task.assignees.forEach { member ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TextMonogram(
                                text = member.firstName.take(1).uppercase(),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = member.firstName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Evidences
                EvidenceView(imageBytes = uiState.evidenceBytes)

                // Related tasks / subtasks
                if (task.relatedTasks.isNotEmpty()) {
                    Text(
                        text = "Related tasks / subtasks",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    task.relatedTasks.forEach { related ->
                        RelatedTaskCard(
                            related = related,
                            onClick = { viewModel.onRelatedTaskClick(related) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(120.dp))
            }
        }

        TaskActionsFabMenu(
            expanded = uiState.isFabMenuExpanded,
            onToggle = viewModel::onFabMenuToggle,
            onAction = { action ->
                if (action == TaskAction.EDIT_TASK) {
                    viewModel.onFabMenuDismiss()
                    onEditTask(uiState.task.id)
                } else {
                    viewModel.onTaskAction(action)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 104.dp)
        )
    }
}


@Composable
private fun TaskActionsFabMenu(
    expanded: Boolean,
    onToggle: () -> Unit,
    onAction: (TaskAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val fabContainerColor = MaterialTheme.colorScheme.primary
    val fabContentColor = MaterialTheme.colorScheme.primaryContainer

    FloatingActionButtonMenu(
        expanded = expanded,
        modifier = modifier,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = { onToggle() },
                containerColor = { fabContainerColor }
            ) {
                Icon(
                    imageVector = if (checkedProgress > 0.5f) {
                        MaterialSymbols.Outlined.Close
                    } else {
                        MaterialSymbols.Outlined.Star
                    },
                    contentDescription = if (expanded) "Close task actions" else "Task actions",
                    tint = fabContentColor
                )
            }
        }
    ) {
        TaskAction.entries.forEach { action ->
            FloatingActionButtonMenuItem(
                onClick = { onAction(action) },
                icon = { Icon(imageVector = action.icon, contentDescription = null) },
                text = {
                    Text(
                        text = action.label,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

private val TaskAction.icon: ImageVector
    get() = when (this) {
        TaskAction.MARK_AS_COMPLETE -> MaterialSymbols.Outlined.Check
        TaskAction.EDIT_TASK -> MaterialSymbols.Outlined.Edit
        TaskAction.DELETE_TASK -> MaterialSymbols.Outlined.Delete
        TaskAction.ASK_FOR_HELP -> MaterialSymbols.Outlined.Chat
        TaskAction.MARK_AS_STARTED -> MaterialSymbols.Outlined.Star
        TaskAction.ASSIGN_TIME_SLOT -> MaterialSymbols.Outlined.Stars
    }

@Composable
private fun AssistiveChip(
    icon: ImageVector,
    label: String,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .border(1.dp, contentColor, RoundedCornerShape(100.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = contentColor)
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}

/**
 * The "Scheduled" card, with a calendar header, a deadline row with EDIT, a divider, and a
 * reminder row with a switch.
 */
@Composable
private fun ScheduledCard(
    deadline: String,
    reminder: Reminder?,
    onReminderToggle: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = MaterialSymbols.Outlined.Calendar_month,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Scheduled",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LabeledRow(
                label = "Deadline",
                value = deadline
            ) {
                TextButton(onClick = onEditClick) { Text("EDIT") }
            }

            if (reminder != null) {
                HorizontalDivider()

                LabeledRow(
                    label = "Reminder",
                    value = reminder.label
                ) {
                    Switch(checked = reminder.enabled, onCheckedChange = onReminderToggle)
                }
            }
        }
    }
}


@Composable
private fun LabeledRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        trailing()
    }
}


@Composable
private fun RelatedTaskCard(
    related: RelatedTask,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        overlineContent = if (related.needsHelp) {
            { Text("Needs Help.") }
        } else {
            null
        },
        headlineContent = {
            Text(
                text = related.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Column {
                val due = taskDue(related.deadline)
                Text(due?.text ?: related.status.label)
            }
        },
        leadingContent = {
            TextMonogram(text = related.title.take(1).uppercase())
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ViewTaskScreenPreview() {
    JuggleTheme {
        ViewTaskScreen()
    }
}
