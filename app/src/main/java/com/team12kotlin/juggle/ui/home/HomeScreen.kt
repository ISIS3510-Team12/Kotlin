package com.team12kotlin.juggle.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Checklist
import com.composables.icons.materialsymbols.outlined.Circle_notifications
import com.composables.icons.materialsymbols.outlined.List
import com.team12kotlin.juggle.ui.components.OverviewCard
import com.team12kotlin.juggle.ui.dto.Notification
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.navbar.NavigationDestination
import com.team12kotlin.juggle.ui.tasks.TaskCard
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun HomeScreen (
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onProfileClick: (NavigationDestination) -> Unit = {},
    onTaskClick: (Task) -> Unit = {},
    onNotificationClick: (Notification) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val startCorner = 20.dp
    val endCorner = 20.dp
    Column(
        modifier = modifier
            .padding(8.dp)
            .fillMaxSize()
            .padding(start = 26.dp, top = 0.dp, end = 26.dp, bottom = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier=modifier.padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.Start,
        ) {
            Column() {
                Text(
                    text = "Hi " + uiState.user.firstName + "!",
                    style = MaterialTheme.typography.displayMedium
                )
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Saturday, September 12, 2026",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(
                        onClick = { },
                        modifier = modifier
                            .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp),
                        shape = RoundedCornerShape(
                            topStart = startCorner,
                            bottomStart = startCorner,
                            topEnd = endCorner,
                            bottomEnd = endCorner
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text="Quick Actions",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleLarge
            )
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OverviewCard(
                    icon = MaterialSymbols.Outlined.Checklist,
                    title = "Tasks",
                    count = uiState.tasks.size
                )
                OverviewCard(
                    icon = MaterialSymbols.Outlined.Circle_notifications,
                    title = "Notifications",
                    count = uiState.notifications.size
                )
            }
        }
        Column(
            modifier = modifier.fillMaxWidth()
                .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp),
        ) {
            uiState.upcomingTasks.forEach { task ->
                TaskCard(
                    task = task,
                    onClick = { onTaskClick(task) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    JuggleTheme() {
        HomeScreen()
    }
}