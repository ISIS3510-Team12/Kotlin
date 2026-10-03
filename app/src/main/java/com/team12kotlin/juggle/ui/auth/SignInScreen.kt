package com.team12kotlin.juggle.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team12kotlin.juggle.ui.telemetry.ScreenName
import com.team12kotlin.juggle.ui.telemetry.TrackScreenLoad
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Arrow_back
import com.team12kotlin.juggle.ui.components.AuthTextField
import com.team12kotlin.juggle.ui.components.GoogleSignInButton
import com.team12kotlin.juggle.ui.components.OrDivider
import com.team12kotlin.juggle.ui.components.PasswordField
import com.team12kotlin.juggle.ui.components.PillButton
import com.team12kotlin.juggle.data.auth.requestGoogleIdToken
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSignInClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrackScreenLoad(ScreenName.SignIn)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun handleGoogleSignIn() {
        scope.launch {
            try {
                viewModel.onGoogleSignIn(requestGoogleIdToken(context))
            } catch (error: Throwable) {
                viewModel.onGoogleSignInError(error)
            }
        }
    }

    LaunchedEffect(uiState.navigateToSuccess) {
        if (uiState.navigateToSuccess) {
            onSignInClick()
            viewModel.onNavigatedToSuccess()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Sign In") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email",
                keyboardType = KeyboardType.Email,
                isError = uiState.emailError != null,
                supportingText = uiState.emailError
            )

            Spacer(modifier = Modifier.height(4.dp))

            PasswordField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Password",
                isError = uiState.passwordError != null,
                supportingText = uiState.passwordError
            )

            Spacer(modifier = Modifier.height(20.dp))

            PillButton(
                text = "Sign In",
                onClick = viewModel::onSignInSubmit,
                enabled = uiState.canSubmit
            )

            uiState.authError?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OrDivider()

            Spacer(modifier = Modifier.height(16.dp))

            GoogleSignInButton(onClick = { handleGoogleSignIn() })

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Don't have an account?",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            PillButton(text = "Sign Up", onClick = onSignUpClick)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 414, heightDp = 892)
@Composable
private fun SignInScreenPreview() {
    JuggleTheme {
        SignInScreen()
    }
}
