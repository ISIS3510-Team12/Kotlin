package com.team12kotlin.juggle.ui.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Warning
import com.team12kotlin.juggle.ui.components.OfflineActionNotice
import com.team12kotlin.juggle.ui.components.SupportingTextWithCounter
import com.team12kotlin.juggle.ui.tasks.create.DatePickerField
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import com.team12kotlin.juggle.ui.utils.ProjectFieldLimits

@Composable
fun StatSummary(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProjectProgressBar(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val fraction = if (total <= 0) 0f else (completed.toFloat() / total.toFloat()).coerceIn(0f, 1f)

    LinearProgressIndicator(
        progress = { fraction },
        modifier = modifier,
        trackColor = MaterialTheme.colorScheme.secondaryContainer,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
fun PaceWarningBanner(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = MaterialSymbols.Outlined.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun ProjectFormContent(
    title: String = "",
    subtitle: String = "",
    nameValue: String = "",
    onNameChange: (String) -> Unit = {},
    descriptionValue: String = "",
    onDescriptionChange: (String) -> Unit = {},
    deadlineValue: String = "",
    onDeadlineChange: (String) -> Unit = {},
    isOffline: Boolean = false,
) {
    Column {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = nameValue,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Project name") },
                placeholder = { Text("Enter project name") },
                supportingText = {
                    SupportingTextWithCounter(
                        text = "",
                        length = nameValue.length,
                        maxLength = ProjectFieldLimits.NAME_MAX_LENGTH
                    )
                }
            )
            OutlinedTextField(
                value = descriptionValue,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Description") },
                placeholder = { Text("Describe the project") },
                supportingText = {
                    SupportingTextWithCounter(
                        text = "Required",
                        length = descriptionValue.length,
                        maxLength = ProjectFieldLimits.DESCRIPTION_MAX_LENGTH
                    )
                }
            )
            DatePickerField(
                value = deadlineValue,
                onDateSelected = onDeadlineChange,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OfflineActionNotice(isOffline = isOffline)
    }
}

@Preview(showBackground = true)
@Composable
private fun StatSummaryPreview() {
    JuggleTheme {
        StatSummary(label = "Progress", value = "6 of 10 tasks complete")
    }
}

@Preview(showBackground = true)
@Composable
private fun ProjectProgressBarPreview() {
    JuggleTheme {
        ProjectProgressBar(completed = 6, total = 10, modifier = Modifier.fillMaxWidth())
    }
}

@Preview(showBackground = true)
@Composable
private fun PaceWarningBannerPreview() {
    JuggleTheme {
        PaceWarningBanner(
            title = "Pace too slow",
            message = "Your current pace won't complete all planned tasks before the deadline."
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProjectFormContentPreview() {
    JuggleTheme {
        ProjectFormContent(
            title = "Create a new project",
            subtitle = "Add the essentials now. You can update them later."
        )
    }
}
