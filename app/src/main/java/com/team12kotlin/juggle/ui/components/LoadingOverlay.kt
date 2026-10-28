package com.team12kotlin.juggle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/** Draws a spinner over the content it is placed on top of. */
@Composable
fun LoadingOverlay(isLoading: Boolean, modifier: Modifier = Modifier) {
    if (!isLoading) return
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
