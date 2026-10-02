package com.team12kotlin.juggle.ui.groups.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Arrow_back
import com.composables.icons.materialsymbols.outlined.Check
import com.composables.icons.materialsymbols.outlined.Delete
import com.composables.icons.materialsymbols.outlined.Search
import com.team12kotlin.juggle.ui.dto.User
import com.team12kotlin.juggle.ui.components.MemberSelectorItem
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun CreateGroupScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateGroupViewModel = viewModel(),
    goBack: () -> Unit = {},
    onCreated: (notice: String?) -> Unit = { goBack() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.created) {
        if (uiState.created) onCreated(uiState.notice)
    }
    val searchBarState = rememberSearchBarState()
    val searchTextFieldState = rememberTextFieldState(uiState.query)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "New Group") },
                navigationIcon = {
                    IconButton(onClick = goBack) {
                        Icon(
                            imageVector = MaterialSymbols.Outlined.Arrow_back,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, top = 0.dp, end = 10.dp, bottom = 0.dp),
                value = uiState.name,
                supportingText = { Text("This will be the main name of the group") },
                onValueChange = viewModel::onNameChange,
                label = { Text("Group Name") },
            )
            OutlinedTextField(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, top = 0.dp, end = 10.dp, bottom = 0.dp),
                value = uiState.description,
                supportingText = { Text("This will be the description of the group") },
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Group Description") },
            )
        }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 0.dp, top = 5.dp, end = 0.dp, bottom = 0.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Bottom),
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Text(
                text = "Add people",
                modifier = modifier
                    .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp),
                style = MaterialTheme.typography.headlineMedium
            )
        }

        SearchBar(
            state = searchBarState,
            inputField = {
                SearchBarDefaults.InputField(
                    searchBarState = searchBarState,
                    textFieldState = searchTextFieldState,
                    onSearch = { viewModel.onSearch(it) },
                    placeholder = {
                        Text("Search for a user...")
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = MaterialSymbols.Outlined.Search,
                            contentDescription = "Search"
                        )
                    }
                )
            },
            modifier = Modifier.padding(start=20.dp, top=5.dp, end=20.dp, bottom = 10.dp).fillMaxWidth()
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start,
        ) {
            uiState.filteredAvailableMembers.forEach { member ->
                MemberSelectorItem(
                    member = member,
                    checkedState = member.userId in uiState.selectedIds,
                    onChecked = viewModel::onToggleMember
                )
            }
        }

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        Row(
            modifier =modifier
                .fillMaxWidth()
                .padding(start = 10.dp, top = 20.dp, end = 0.dp, bottom = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = viewModel::onCreateGroup,
                enabled = uiState.canCreate
            ) {
                Row(
                    modifier = modifier
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Outlined.Check,
                        contentDescription = "Check"
                    )
                    Text(text = "Create Group")
                }
            }
            OutlinedButton(
                onClick = goBack
            ) {
                Row(
                    modifier = modifier
                        .padding(start = 12.dp, top = 0.dp, end = 12.dp, bottom = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Outlined.Delete,
                        contentDescription = "Cancel"
                    )
                    Text(text = "Cancel")
                }
            }
        }

        Spacer(modifier = Modifier.height(136.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateGroupsScreensPreview() {
    JuggleTheme() {
        CreateGroupScreen()
    }
}