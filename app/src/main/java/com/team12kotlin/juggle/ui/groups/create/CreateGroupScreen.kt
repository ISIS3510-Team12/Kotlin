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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team12kotlin.juggle.ui.telemetry.ScreenName
import com.team12kotlin.juggle.ui.telemetry.TrackScreenLoad
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Check
import com.composables.icons.materialsymbols.outlined.Delete
import com.composables.icons.materialsymbols.outlined.Search
import com.team12kotlin.juggle.ui.components.BackTopBar
import com.team12kotlin.juggle.ui.components.ErrorCard
import com.team12kotlin.juggle.ui.components.LoadingContent
import com.team12kotlin.juggle.ui.components.MemberSelectorItem
import com.team12kotlin.juggle.ui.components.SupportingTextWithCounter
import com.team12kotlin.juggle.ui.components.OfflineActionNotice
import com.team12kotlin.juggle.ui.connectivity.rememberIsOnline
import com.team12kotlin.juggle.ui.dto.User
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import com.team12kotlin.juggle.ui.utils.GroupFieldLimits

@Composable
fun CreateGroupScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateGroupViewModel = viewModel(),
    goBack: () -> Unit = {},
    onCreated: (notice: String?) -> Unit = { goBack() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrackScreenLoad(ScreenName.CreateGroup, uiState.isLoading)

    LaunchedEffect(uiState.created) {
        if (uiState.created) onCreated(uiState.notice)
    }

    CreateGroupContent(
        uiState = uiState,
        modifier = modifier,
        goBack = goBack,
        onNameChange = viewModel::onNameChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onSearch = viewModel::onSearch,
        onToggleMember = viewModel::onToggleMember,
        onCreateGroup = viewModel::onCreateGroup,
    )
}

@Composable
fun CreateGroupContent(
    uiState: CreateGroupUiState,
    modifier: Modifier = Modifier,
    goBack: () -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onDescriptionChange: (String) -> Unit = {},
    onSearch: (String) -> Unit = {},
    onToggleMember: (User) -> Unit = {},
    onCreateGroup: () -> Unit = {},
) {
    val isOffline = rememberIsOnline() == false
    val searchBarState = rememberSearchBarState()
    val searchTextFieldState = rememberTextFieldState(uiState.query)

    Scaffold(
        modifier = modifier,
        topBar = {
            BackTopBar(title = "New Group", onBack = goBack)
        }
    ) { innerPadding ->
        LoadingContent(isLoading = uiState.isLoading) {
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
                        supportingText = {
                            SupportingTextWithCounter(
                                text = "This will be the main name of the group",
                                length = uiState.name.length,
                                maxLength = GroupFieldLimits.NAME_MAX_LENGTH
                            )
                        },
                        onValueChange = onNameChange,
                        label = { Text("Group Name") },
                    )
                    OutlinedTextField(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, top = 0.dp, end = 10.dp, bottom = 0.dp),
                        value = uiState.description,
                        supportingText = {
                            SupportingTextWithCounter(
                                text = "This will be the description of the group",
                                length = uiState.description.length,
                                maxLength = GroupFieldLimits.DESCRIPTION_MAX_LENGTH
                            )
                        },
                        onValueChange = onDescriptionChange,
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
                            onSearch = { onSearch(it) },
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
                            onChecked = onToggleMember
                        )
                    }
                }

                uiState.errorMessage?.let { message ->
                    ErrorCard(message = message)
                }

                OfflineActionNotice(isOffline = isOffline)

                Row(
                    modifier =modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, top = 20.dp, end = 0.dp, bottom = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = onCreateGroup,
                        enabled = uiState.canCreate && !isOffline
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
}

@Preview(showBackground = true)
@Composable
private fun CreateGroupsScreensPreview() {
    JuggleTheme {
        CreateGroupContent(
            uiState = CreateGroupUiState().copy(
                availableMembers = mutableListOf(
                    User(userId = "u1", firstName = "Diego", email = "d.munevar@uniandes.edu.co", major = "Comp Sci"),
                    User(userId = "u2", firstName = "Manuela", email = "m.loveral@uniandes.edu.co", major = "Comp Sci"),
                    User(userId = "u3", firstName = "Shaiel", email = "sm.jimenez@uniandes.edu.co", major = "Comp Sci")
                )
            )
        )
    }
}