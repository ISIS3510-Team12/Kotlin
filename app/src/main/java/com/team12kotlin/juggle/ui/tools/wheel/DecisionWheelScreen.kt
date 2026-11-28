package com.team12kotlin.juggle.ui.tools.wheel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Add
import com.composables.icons.materialsymbols.outlined.Chevron_right
import com.composables.icons.materialsymbols.outlined.List_alt
import com.team12kotlin.juggle.ui.components.BackTopBar
import com.team12kotlin.juggle.ui.components.ErrorCard
import com.team12kotlin.juggle.ui.components.LoadingContent
import com.team12kotlin.juggle.ui.dto.Wheel
import com.team12kotlin.juggle.ui.telemetry.ScreenName
import com.team12kotlin.juggle.ui.telemetry.TrackScreenLoad
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@Composable
fun DecisionWheelScreen(
    modifier: Modifier = Modifier,
    viewModel: DecisionWheelViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onWheelClick: (Wheel) -> Unit = {},
    onCreateWheelClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrackScreenLoad(ScreenName.DecisionWheel, uiState.isLoading)

    DecisionWheelContent(
        uiState = uiState,
        modifier = modifier,
        onBackClick = onBackClick,
        onRetry = viewModel::loadWheels,
        onWheelClick = onWheelClick,
        onCreateWheelClick = onCreateWheelClick
    )
}

@Composable
private fun DecisionWheelContent(
    uiState: DecisionWheelUiState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onRetry: () -> Unit = {},
    onWheelClick: (Wheel) -> Unit = {},
    onCreateWheelClick: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = { BackTopBar(title = "Decision Wheel", onBack = onBackClick) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateWheelClick,
                modifier = Modifier.padding(bottom = 104.dp),
                icon = {
                    Icon(
                        MaterialSymbols.Outlined.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                text = {
                    Text(
                        text = "Create wheel",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    ) { innerPadding ->
        LoadingContent(isLoading = uiState.isLoading) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 136.dp)
            ) {
                item {
                    Text(
                        text = "Your wheels",
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                uiState.errorMessage?.let { message ->
                    item {
                        ErrorCard(
                            message = message,
                            onRetry = onRetry,
                            modifier = Modifier.padding(horizontal = 18.dp)
                        )
                    }
                }
                if (uiState.wheels.isEmpty() && uiState.errorMessage == null) {
                    item {
                        Text(
                            text = "Create your first wheel and let it decide for you.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
                        )
                    }
                }
                items(uiState.wheels, key = { it.id }) { wheel ->
                    WheelCard(wheel = wheel, onClick = { onWheelClick(wheel) })
                }
            }
        }
    }
}

@Composable
private fun WheelCard(
    wheel: Wheel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = MaterialSymbols.Outlined.List_alt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wheel.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${wheel.optionCount} ${if (wheel.optionCount == 1) "option" else "options"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = MaterialSymbols.Outlined.Chevron_right,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DecisionWheelScreenPreview() {
    JuggleTheme {
        DecisionWheelContent(
            uiState = DecisionWheelUiState(
                wheels = listOf(
                    Wheel(1, "What should we eat?", 6),
                    Wheel(2, "Movie night", 1)
                ),
                isLoading = false
            )
        )
    }
}
