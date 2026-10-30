package com.team12kotlin.juggle.ui.topbar

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlinedfilled.Groups
import com.team12kotlin.juggle.R
import com.team12kotlin.juggle.ui.components.OfflineBanner
import com.team12kotlin.juggle.ui.connectivity.RefreshOnReconnect
import com.team12kotlin.juggle.ui.connectivity.rememberIsOnline
import com.team12kotlin.juggle.ui.components.TextMonogram
import com.team12kotlin.juggle.ui.navbar.NavigationDestination
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    modifier: Modifier = Modifier,
    viewModel: TopBarViewModel = viewModel(),
    showGroupIcon: Boolean? = false,
    onMenuClick: () -> Unit = {},
    onProfileClick: (NavigationDestination) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isOnline = rememberIsOnline()

    RefreshOnReconnect(isOnline = isOnline, onReconnect = viewModel::refresh)

    AppTopBarContent(
        modifier = modifier,
        userInitial = uiState.userInitial,
        showGroupIcon = showGroupIcon ?: uiState.showGroupIcon,
        isOnline = isOnline,
        onMenuClick = {
            viewModel.onMenuClick()
            onMenuClick()
        },
        onProfileClick = {
            viewModel.onProfileClick()
            onProfileClick(NavigationDestination.Profile)
        }
    )
}

@Composable
private fun TopBarTitleLogo(
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(if (darkTheme) R.drawable.logo_dark else R.drawable.logo_light),
        contentDescription = "Juggle",
        modifier = modifier.height(35.dp),
        contentScale = ContentScale.Fit
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBarContent(
    userInitial: String,
    showGroupIcon: Boolean,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme(),
    isOnline: Boolean? = true,
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val colors = TopAppBarDefaults.topAppBarColors(
        titleContentColor = MaterialTheme.colorScheme.primary
    )
    Column(modifier = modifier) {
        CenterAlignedTopAppBar(
            colors = colors,
            title = { TopBarTitleLogo(darkTheme = darkTheme) },
            navigationIcon = {
                if (showGroupIcon) {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = MaterialSymbols.OutlinedFilled.Groups,
                            contentDescription = "Menu"
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = onProfileClick) {
                    TextMonogram(userInitial)
                }
            }
        )
        if (isOnline == false) {
            OfflineBanner(isOnline = false)
        }
    }
}

@Preview
@Composable
private fun AppTopBarWithIconLightPreview() {
    JuggleTheme(darkTheme = false) {
        AppTopBarContent(
            userInitial = "A",
            showGroupIcon = true,
            darkTheme = false
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppTopBarWithIconDarkPreview() {
    JuggleTheme(darkTheme = true) {
        AppTopBarContent(
            userInitial = "A",
            showGroupIcon = true,
            darkTheme = true
        )
    }
}

@Preview
@Composable
private fun AppTopBarWithoutIconLightPreview() {
    JuggleTheme(darkTheme = false) {
        AppTopBarContent(
            userInitial = "A",
            showGroupIcon = false,
            darkTheme = false
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppTopBarWithoutIconDarkPreview() {
    JuggleTheme(darkTheme = true) {
        AppTopBarContent(
            userInitial = "A",
            showGroupIcon = false,
            darkTheme = true
        )
    }
}
