package com.team12kotlin.juggle.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Arrow_back

/** Standard top bar with a back arrow and a title. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = MaterialSymbols.Outlined.Arrow_back,
                    contentDescription = "Back"
                )
            }
        }
    )
}
