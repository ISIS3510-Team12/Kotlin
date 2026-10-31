package com.team12kotlin.juggle.ui.groups

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team12kotlin.juggle.ui.telemetry.ScreenName
import com.team12kotlin.juggle.ui.telemetry.TrackScreenLoad
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Add
import com.composables.icons.materialsymbols.outlined.Search
import com.team12kotlin.juggle.ui.components.ErrorCard
import com.team12kotlin.juggle.ui.components.LoadingContent
import com.team12kotlin.juggle.ui.connectivity.RefreshOnReconnect
import com.team12kotlin.juggle.ui.connectivity.rememberIsOnline
import com.team12kotlin.juggle.ui.dto.Group
import com.team12kotlin.juggle.ui.navbar.NavigationDestination
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import com.team12kotlin.juggle.ui.topbar.AppTopBar
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun GroupsScreen (
    modifier: Modifier = Modifier,
    viewModel: GroupsViewModel = viewModel(),
    onNavigateToCreateGroup: () -> Unit = {},
    onProfileClick: (NavigationDestination) -> Unit = {},
    onGroupClick: (Group) -> Unit = {},
    notice: String? = null,
    onNoticeShown: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrackScreenLoad(ScreenName.Groups, uiState.isLoading)
    val isOnline = rememberIsOnline()
    RefreshOnReconnect(isOnline = isOnline, onReconnect = viewModel::loadGroups)

    LifecycleResumeEffect(Unit) {
        viewModel.loadGroups()
        onPauseOrDispose { }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(notice) {
        if (notice != null) {
            snackbarHostState.showSnackbar(notice)
            onNoticeShown()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 104.dp))
        },
        topBar = {
            AppTopBar(
                onProfileClick = onProfileClick,
                showCachedData = isOnline == false && uiState.groups.isNotEmpty()
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.onCreateGroup()
                    onNavigateToCreateGroup()
                },
                modifier = Modifier.padding(bottom = 104.dp),
                icon = {
                    Icon(
                        MaterialSymbols.Outlined.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                text = {
                    Text(
                        text = "Create Group",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    ) { innerPadding ->
        LoadingContent(isLoading = uiState.isLoading) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = viewModel::onQueryChange,
                        singleLine = true,
                        shape = RoundedCornerShape(100.dp),
                        placeholder = { Text("Search for a group...") },
                        trailingIcon = {
                            Icon(
                                imageVector = MaterialSymbols.Outlined.Search,
                                contentDescription = "Search"
                            )
                        },
                        modifier = Modifier
                            .padding(start = 20.dp, top = 5.dp, end = 20.dp, bottom = 20.dp)
                            .fillMaxWidth()
                    )
                }
                uiState.errorMessage?.let { message ->
                    item {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ErrorCard(
                                message = message,
                                onRetry = viewModel::loadGroups
                            )
                        }
                    }
                }
                if (!uiState.isLoading && !uiState.loadFailed && uiState.filteredGroups.isEmpty()) {
                    item {
                        Text(
                            text = "No groups yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                }
                items(uiState.filteredGroups, key = { it.id }) { group ->
                    GroupCard(
                        group = group,
                        onClick = { onGroupClick(group) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupsScreenPreview() {
    JuggleTheme {
        GroupsScreen()
    }
}
