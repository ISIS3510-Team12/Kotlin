package com.team12kotlin.juggle.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Add_circle
import com.composables.icons.materialsymbols.outlined.Check_box
import com.composables.icons.materialsymbols.outlinedfilled.Edit
import com.team12kotlin.juggle.ui.dto.Notification
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@Composable
fun NotificationCard(
    notification: Notification,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    createIcon: ImageVector = MaterialSymbols.Outlined.Add_circle,
    editIcon: ImageVector = MaterialSymbols.OutlinedFilled.Edit,
    completeIcon: ImageVector = MaterialSymbols.Outlined.Check_box,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (notification.type == "create") {
                IconMonogram(
                    icon = createIcon,
                    contentDescription = "Create",
                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    contentColor = MaterialTheme.colorScheme.primaryContainer
                )
            } else if (notification.type == "edit") {
                IconMonogram(
                    icon = editIcon,
                    contentDescription = "Edit",
                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    contentColor = MaterialTheme.colorScheme.primaryContainer
                )
            } else if (notification.type == "complete") {
                IconMonogram(
                    icon = completeIcon,
                    contentDescription = "Complete",
                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    contentColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    notification.origin,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    notification.date,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview
@Composable
private fun CompletedNotificationCardPreview() {
    JuggleTheme {
        NotificationCard(Notification(id=1, title="Finished task", date="Thursday, September 10 2026 8:00am", origin = "Group dev", type = "complete"))
    }
}

@Preview
@Composable
private fun EditedNotificationCardPreview(){
    JuggleTheme {
        NotificationCard(Notification(id=1, title="Edited task", date="Thursday, September 10 2026 8:00am", origin = "Group dev", type = "edit"))
    }
}

@Preview
@Composable
private fun CreatedNotificationCardPreview() {
    JuggleTheme {
        NotificationCard(Notification(id=1, title="Create task", date="Thursday, September 10 2026 8:00am", origin = "Group dev", type = "create"))
    }
}
