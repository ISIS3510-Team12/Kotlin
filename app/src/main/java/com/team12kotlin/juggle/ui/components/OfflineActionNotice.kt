package com.team12kotlin.juggle.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun OfflineActionNotice(
    isOffline: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isOffline) return
    Text(
        text = "You're offline. Reconnect to make changes.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier
    )
}
