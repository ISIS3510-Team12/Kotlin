package com.team12kotlin.juggle.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemColors
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Add
import com.composables.icons.materialsymbols.outlined.Checklist
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@Composable
fun GroupsDrawer(
    groups: List<DrawerItem>,
    modifier: Modifier = Modifier,
    onGroupClick: (DrawerItem) -> Unit = {},
    onAllTasksClick: () -> Unit = {},
    onNewGroupClick: () -> Unit = {}
) {
    ModalDrawerSheet(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DrawerSectionTitle("Your groups")

            groups.forEach { group ->
                NavigationDrawerItem(
                    label = { Text(group.name) },
                    selected = group.selected,
                    onClick = { onGroupClick(group) },
                    badge = if (group.pendingTasks > 0) {
                        { Text("${group.pendingTasks} pending tasks") }
                    } else {
                        null
                    },
                    colors = drawerItemColors()
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            NavigationDrawerItem(
                label = { Text("All tasks") },
                selected = false,
                onClick = onAllTasksClick,
                icon = {
                    Icon(
                        imageVector = MaterialSymbols.Outlined.Checklist,
                        contentDescription = null
                    )
                }
            )

            NavigationDrawerItem(
                label = { Text("New Group") },
                selected = false,
                onClick = onNewGroupClick,
                icon = {
                    Icon(
                        imageVector = MaterialSymbols.Outlined.Add,
                        contentDescription = null
                    )
                }
            )
        }
    }
}

@Composable
private fun DrawerSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun drawerItemColors(): NavigationDrawerItemColors = NavigationDrawerItemDefaults.colors(
    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
    selectedBadgeColor = MaterialTheme.colorScheme.onSecondaryContainer
)

@Preview(showBackground = true)
@Composable
private fun GroupsDrawerPreview() {
    JuggleTheme {
        GroupsDrawer(
            groups = listOf(
                DrawerItem(name = "App Devs", pendingTasks = 67, selected = true),
                DrawerItem(name = "Group 1"),
                DrawerItem(name = "Group 2")
            )
        )
    }
}
