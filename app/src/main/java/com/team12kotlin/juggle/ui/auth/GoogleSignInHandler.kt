package com.team12kotlin.juggle.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.team12kotlin.juggle.data.auth.requestGoogleIdToken
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch

@Immutable
data class GoogleSignInUiState(
    val isLoading: Boolean,
    val onClick: () -> Unit
)

@Composable
fun rememberGoogleSignInHandler(
    isOffline: Boolean,
    onIdToken: (String) -> Unit,
    onError: (Throwable) -> Unit
): GoogleSignInUiState {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val onClick: () -> Unit = {
        if (!isOffline && !isLoading) {
            isLoading = true
            scope.launch {
                try {
                    onIdToken(requestGoogleIdToken(context))
                } catch (error: TimeoutCancellationException) {
                    onError(error)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    onError(error)
                } finally {
                    isLoading = false
                }
            }
        }
    }

    return GoogleSignInUiState(isLoading, onClick)
}
