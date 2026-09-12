package com.team12kotlin.juggle.ui.navbar

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@Composable
fun AppNavigationBar(
    modifier: Modifier = Modifier,
    viewModel: NavbarViewModel = viewModel(),
    onDestinationSelected: (NavigationDestination) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NavigationBar(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(80.dp)),
        windowInsets = WindowInsets(0.dp)
    ) {
        uiState.destinations.forEach { destination ->
            NavigationBarItem(
                selected = destination == uiState.selectedDestination,
                onClick = {
                    viewModel.onDestinationSelected(destination)
                    onDestinationSelected(destination)
                },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.contentDescription
                    )
                },
                label = { Text(destination.label) }
            )
        }
    }
}

@Preview
@Composable
private fun AppNavigationBarPreview() {
    JuggleTheme {
        AppNavigationBar()
    }
}
