package com.team12kotlin.juggle.ui.connectivity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue

/** Runs onReconnect once when connectivity changes from offline to online. */
@Composable
fun RefreshOnReconnect(
    isOnline: Boolean?,
    onReconnect: () -> Unit
) {
    val currentOnReconnect by rememberUpdatedState(onReconnect)
    var wasOffline by remember { mutableStateOf(false) }

    LaunchedEffect(isOnline) {
        when (isOnline) {
            false -> wasOffline = true
            true -> if (wasOffline) {
                wasOffline = false
                currentOnReconnect()
            }
            null -> Unit
        }
    }
}
