package com.team12kotlin.juggle.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LoadingContent(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (isLoading) {
        LoadingOverlay(isLoading = true, modifier = modifier)
    } else {
        content()
    }
}
