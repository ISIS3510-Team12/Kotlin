package com.team12kotlin.juggle.ui.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Add
import com.composables.icons.materialsymbols.outlined.Search
import com.team12kotlin.juggle.ui.dto.Group
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun GroupsScreen (
    modifier: Modifier = Modifier,
    viewModel: GroupsViewModel = viewModel(),
    onNavigateToCreateGroup: () -> Unit = {},
    onGroupClick: (Group) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState(uiState.query)

    Scaffold(
        modifier = modifier,
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SearchBar(
                state = searchBarState,
                inputField = {
                    SearchBarDefaults.InputField(
                        searchBarState = searchBarState,
                        textFieldState = textFieldState,
                        onSearch = { viewModel.onSearch(it) },
                        placeholder = {
                            Text("Search for a task...")
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = MaterialSymbols.Outlined.Search,
                                contentDescription = "Search"
                            )
                        }
                    )
                },
                modifier = Modifier.padding(start=20.dp, top=5.dp, end=20.dp, bottom = 20.dp).fillMaxWidth()
            )
            Column(
            ) {
                uiState.filteredGroups.forEach { group ->
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