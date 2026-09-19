package com.team12kotlin.juggle.ui.groups.edit

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Arrow_back
import com.composables.icons.materialsymbols.outlined.Check
import com.composables.icons.materialsymbols.outlined.Delete
import com.composables.icons.materialsymbols.outlined.Search
import com.team12kotlin.juggle.ui.components.MemberSelectorItem
import com.team12kotlin.juggle.ui.dto.User
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun EditGroupScreen(
    groupId: String,
    modifier: Modifier = Modifier,
    viewModel: EditGroupViewModel = viewModel(),
    goBack: () -> Unit = {}
) {
    LaunchedEffect(groupId) { viewModel.load(groupId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EditGroupContent(
        modifier = modifier,
        name = uiState.name,
        description = uiState.description,
        directory = uiState.filteredDirectory,
        selectedMembers = uiState.selectedMembers,
        onNameChange = viewModel::onNameChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onSearch = viewModel::onSearch,
        onMemberToggled = viewModel::onMemberToggled,
        onSave = {
            viewModel.onSaveGroup()
            goBack()
        },
        goBack = goBack
    )
}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
private fun EditGroupContent(
    name: String,
    description: String,
    directory: List<User>,
    selectedMembers: Set<User>,
    modifier: Modifier = Modifier,
    onNameChange: (String) -> Unit = {},
    onDescriptionChange: (String) -> Unit = {},
    onSearch: (String) -> Unit = {},
    onMemberToggled: (User) -> Unit = {},
    onSave: () -> Unit = {},
    goBack: () -> Unit = {}
) {
    val searchBarState = rememberSearchBarState()
    val searchTextFieldState = rememberTextFieldState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Edit Group") },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = name,
                    supportingText = { Text("This is the current name of the group") },
                    onValueChange = onNameChange,
                    label = { Text("Name") },
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = description,
                    supportingText = { Text("This is the current class of the group") },
                    onValueChange = onDescriptionChange,
                    label = { Text("Class") },
                )
            }

            Text(
                text = "Edit People",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium
            )

            SearchBar(
                state = searchBarState,
                inputField = {
                    SearchBarDefaults.InputField(
                        searchBarState = searchBarState,
                        textFieldState = searchTextFieldState,
                        onSearch = onSearch,
                        placeholder = {
                            Text("Search for users...")
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = MaterialSymbols.Outlined.Search,
                                contentDescription = "Search"
                            )
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start,
            ) {
                directory.forEach { member ->
                    MemberSelectorItem(
                        member = member,
                        checkedState = member in selectedMembers,
                        onChecked = onMemberToggled
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = onSave) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Outlined.Check,
                            contentDescription = "Check"
                        )
                        Text(text = "Edit Group")
                    }
                }
                OutlinedButton(onClick = goBack) {
                    Row(
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp),
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
private fun EditGroupScreenPreview() {
    val members = listOf(
        User(firstName = "Diego Alejandro", email = "d.munevar@uniandes.edu.co", major = "Comp Sci"),
        User(firstName = "Manuela", email = "m.loveral@uniandes.edu.co", major = "Comp Sci"),
        User(firstName = "Shaiel Mateo", email = "sm.jimenez@uniandes.edu.co", major = "Comp Sci"),
        User(firstName = "Juan Diego", email = "jd.perez@uniandes.edu.co", major = "Comp Sci"),
        User(firstName = "Snoopy", email = "snoopy@uniandes.edu.co", major = "Comp Sci")
    )
    JuggleTheme {
        EditGroupContent(
            name = "An amazing group",
            description = "ISIS-3510",
            directory = members,
            selectedMembers = members.toSet()
        )
    }
}
