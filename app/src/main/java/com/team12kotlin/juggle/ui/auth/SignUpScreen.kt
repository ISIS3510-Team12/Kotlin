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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Arrow_back
import com.composables.icons.materialsymbols.outlined.Lock
import com.composables.icons.materialsymbols.outlined.Mail
import com.composables.icons.materialsymbols.outlined.Person
import com.team12kotlin.juggle.ui.components.AuthTextField
import com.team12kotlin.juggle.ui.components.GoogleSignInButton
import com.team12kotlin.juggle.ui.components.OrDivider
import com.team12kotlin.juggle.ui.components.PasswordField
import com.team12kotlin.juggle.ui.components.PillButton
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onGoogleSignInClick: () -> Unit = {},
    onSignInClick: () -> Unit = {}
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Sign Up") },
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

            // Name group.
            AuthTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = "First Name",
                leadingIcon = { Icon(MaterialSymbols.Outlined.Person, contentDescription = null) }
            )
            Spacer(modifier = Modifier.height(4.dp))
            AuthTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = "Last Name",
                leadingIcon = { Icon(MaterialSymbols.Outlined.Person, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Credentials group.
            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                leadingIcon = { Icon(MaterialSymbols.Outlined.Mail, contentDescription = null) },
                keyboardType = KeyboardType.Email
            )
            Spacer(modifier = Modifier.height(4.dp))
            PasswordField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                leadingIcon = { Icon(MaterialSymbols.Outlined.Lock, contentDescription = null) }
            )
            Spacer(modifier = Modifier.height(4.dp))
            PasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm Password",
                leadingIcon = { Icon(MaterialSymbols.Outlined.Lock, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            PillButton(text = "Sign Up", onClick = onSignUpClick)

            Spacer(modifier = Modifier.height(16.dp))

            OrDivider()

            Spacer(modifier = Modifier.height(16.dp))

            GoogleSignInButton(onClick = onGoogleSignInClick)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Already have an account?",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            PillButton(text = "Sign In", onClick = onSignInClick)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 414, heightDp = 892)
@Composable
private fun SignUpScreenPreview() {
    JuggleTheme {
        SignUpScreen()
    }
}
