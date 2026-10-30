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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.composables.icons.materialsymbols.outlined.Mail
import com.composables.icons.materialsymbols.outlined.Person
import com.team12kotlin.juggle.ui.components.BackTopBar
import androidx.compose.foundation.layout.Box
import com.team12kotlin.juggle.ui.components.ErrorCard
import com.team12kotlin.juggle.ui.connectivity.RefreshOnReconnect
import com.team12kotlin.juggle.ui.connectivity.rememberIsOnline
import com.team12kotlin.juggle.ui.components.LoadingContent
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
    RefreshOnReconnect(isOnline = rememberIsOnline(), onReconnect = viewModel::loadUser)

    ProfileInformationContent(
        modifier = modifier,
        uiState = uiState,
        onRetry = viewModel::loadUser,
        goBack = goBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileInformationContent(
    uiState: ProfileInformationUiState,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    goBack: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BackTopBar(title = "Profile Information", onBack = goBack)
        }
    ) { innerPadding ->
        LoadingContent(isLoading = uiState.isLoading) {
            if (uiState.email.isBlank()) {
                uiState.errorMessage?.let { message ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        ErrorCard(message = message, onRetry = onRetry)
                    }
                }
                return@LoadingContent
            }
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
                ReadOnlyField(
                    value = uiState.firstName,
                    label = "First Name"
                )
                ReadOnlyField(
                    value = uiState.lastName,
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
}

@Composable
private fun ReadOnlyField(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = MaterialSymbols.Outlined.Person,
                contentDescription = null
            )
        },
        singleLine = true,
        readOnly = true
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
