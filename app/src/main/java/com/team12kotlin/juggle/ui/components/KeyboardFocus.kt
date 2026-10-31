package com.team12kotlin.juggle.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@Composable
fun Modifier.showKeyboardOnFocus(): Modifier {
    val keyboardController = LocalSoftwareKeyboardController.current
    return onFocusChanged { state ->
        if (state.isFocused) {
            keyboardController?.show()
        }
    }
}
