package com.team12kotlin.juggle.ui.profile.information

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.composables.icons.materialsymbols.outlined.Arrow_back
import com.composables.icons.materialsymbols.outlined.Cancel
import com.composables.icons.materialsymbols.outlined.Mail
import com.composables.icons.materialsymbols.outlined.Person
import com.team12kotlin.juggle.ui.components.TextMonogram
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@Composable
fun ProfileInformationScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileInformationViewModel = viewModel(),
    goBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrackScreenLoad(ScreenName.ProfileInformation, uiState.isLoading)

    ProfileInformationContent(
        modifier = modifier,
        uiState = uiState,
        goBack = goBack,
        onFirstNameChange = viewModel::onFirstNameChange,
        onLastNameChange = viewModel::onLastNameChange
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileInformationContent(
    uiState: ProfileInformationUiState,
    modifier: Modifier = Modifier,
    goBack: () -> Unit = {},
    onFirstNameChange: (String) -> Unit = {},
    onLastNameChange: (String) -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Profile Information") },
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
                .padding(start = 16.dp, top = 12.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextMonogram(
                text = uiState.firstName.take(1).uppercase(),
                size = 56.dp,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
            ClearableField(
                value = uiState.firstName,
                onValueChange = onFirstNameChange,
                label = "First Name"
            )
            ClearableField(
                value = uiState.lastName,
                onValueChange = onLastNameChange,
                label = "Last Name"
            )
            OutlinedTextField(
                value = uiState.email,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        imageVector = MaterialSymbols.Outlined.Mail,
                        contentDescription = null
                    )
                },
                singleLine = true,
                readOnly = true
            )
        }
    }
}

@Composable
private fun ClearableField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = MaterialSymbols.Outlined.Person,
                contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(onClick = { onValueChange("") }) {
                Icon(
                    imageVector = MaterialSymbols.Outlined.Cancel,
                    contentDescription = "Clear $label"
                )
            }
        },
        singleLine = true
    )
}

@Preview(showBackground = true)
@Composable
private fun ProfileInformationScreenPreview() {
    JuggleTheme {
        ProfileInformationContent(
            uiState = ProfileInformationUiState(
                firstName = "John",
                lastName = "Doe",
                email = "test@test.com"
            )
        )
    }
}
