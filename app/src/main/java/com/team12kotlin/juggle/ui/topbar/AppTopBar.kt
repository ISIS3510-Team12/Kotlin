package com.team12kotlin.juggle.ui.topbar

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlinedfilled.Groups
import com.team12kotlin.juggle.ui.components.TextMonogram
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    modifier: Modifier = Modifier,
    viewModel: TopBarViewModel = viewModel(),
    showGroupIcon: Boolean? = null,
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AppTopBarContent(
        modifier = modifier,
        title = uiState.title,
        userInitial = uiState.userInitial,
        showGroupIcon = showGroupIcon ?: uiState.showGroupIcon,
        onMenuClick = {
            viewModel.onMenuClick()
            onMenuClick()
        },
        onProfileClick = {
            viewModel.onProfileClick()
            onProfileClick()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBarContent(
    title: String,
    userInitial: String,
    showGroupIcon: Boolean,
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val colors = TopAppBarDefaults.topAppBarColors(
        titleContentColor = MaterialTheme.colorScheme.primary
    )
    val actions: @Composable () -> Unit = {
        IconButton(onClick = onProfileClick) {
            TextMonogram(userInitial)
        }
    }

    if (showGroupIcon) {
        CenterAlignedTopAppBar(
            modifier = modifier,
            colors = colors,
            title = { Text(title) },
            navigationIcon = {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = MaterialSymbols.OutlinedFilled.Groups,
                        contentDescription = "Menu"
                    )
                }
            },
            actions = { actions() }
        )
    } else {
        // No navigation icon: start-aligned title so text sits to the left.
        TopAppBar(
            modifier = modifier,
            colors = colors,
            title = { Text(title) },
            actions = { actions() }
        )
    }
}

@Preview
@Composable
private fun AppTopBarWithIconPreview() {
    JuggleTheme {
        AppTopBarContent(
            title = "[Cool App Icon/Logo here]",
            userInitial = "A",
            showGroupIcon = true
        )
    }
}

@Preview
@Composable
private fun AppTopBarWithoutIconPreview() {
    JuggleTheme {
        AppTopBarContent(
            title = "[Cool App Icon/Logo here]",
            userInitial = "A",
            showGroupIcon = false
        )
    }
}
