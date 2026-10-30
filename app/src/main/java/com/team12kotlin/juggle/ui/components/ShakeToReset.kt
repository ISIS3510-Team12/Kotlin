package com.team12kotlin.juggle.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.sensors.ShakeEffect
import com.team12kotlin.juggle.ui.theme.JuggleTheme

/** Asks for confirmation when the device is shaken and calls [onReset] if the user accepts. */
@Composable
fun ShakeToReset(
    enabled: Boolean = true,
    onReset: () -> Unit
) {
    val shakeEnabled by Dependencies.shakeEffectStore.enabledFlow.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    // The sensor is released while the dialog is open, so a shake can't stack another one
    ShakeEffect(enabled = enabled && shakeEnabled && !showDialog) {
        showDialog = true
    }

    if (showDialog) {
        ResetFormDialog(
            onConfirm = {
                showDialog = false
                onReset()
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun ResetFormDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset form?") },
        text = { Text("The information you entered will be cleared.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Reset") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ResetFormDialogPreview() {
    JuggleTheme {
        ResetFormDialog(onConfirm = {}, onDismiss = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ResetFormDialogPreviewDark() {
    JuggleTheme(darkTheme = true) {
        ResetFormDialog(onConfirm = {}, onDismiss = {})
    }
}
