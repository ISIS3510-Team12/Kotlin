package com.team12kotlin.juggle.ui.tools.pomodoro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Arrow_circle_down
import com.composables.icons.materialsymbols.outlined.Play_arrow
import com.composables.icons.materialsymbols.outlined.Present_to_all
import com.composables.icons.materialsymbols.outlined.Reset_focus
import com.composables.icons.materialsymbols.outlined.Reset_iso
import com.composables.icons.materialsymbols.outlined.Reset_settings
import com.composables.icons.materialsymbols.outlined.Reset_shadow
import com.composables.icons.materialsymbols.outlined.Restart_alt
import com.composables.icons.materialsymbols.outlined.Skip_next
import com.composables.icons.materialsymbols.outlinedfilled.Circle
import com.composables.icons.materialsymbols.outlinedfilled.Square
import com.team12kotlin.juggle.ui.components.BackTopBar
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@Composable
fun PomodoroScreen(
    modifier: Modifier = Modifier,
    viewModel: PomodoroViewModel = viewModel(),
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PomodoroScreenContent(
        uiState=uiState,
        toggleTimer = viewModel::toggleTimer,
        nextPhase = viewModel::onNextPhase,
        reset = viewModel::reset,
    )
}

@Composable
internal fun PomodoroScreenContent(
    uiState: PomodoroUiState,
    modifier: Modifier = Modifier,
    toggleTimer: () -> Unit = {},
    reset: () -> Unit = {},
    nextPhase: () -> Unit = {},
    onBackClick: () -> Unit = {}
){
    Scaffold(
        modifier = modifier,
        topBar = { BackTopBar(title = "Pomodoro Timer", onBack = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = modifier.fillMaxSize().padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Study Session",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Stay focused on your task",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { uiState.progress },
                    modifier = Modifier.size(270.dp),
                    strokeWidth = 15.dp,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    trackColor = MaterialTheme.colorScheme.primary
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "%02d:%02d".format(uiState.remainingSeconds / 60, uiState.remainingSeconds % 60),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (session in 1..uiState.completedSessions) {
                            Icon(
                                imageVector = MaterialSymbols.OutlinedFilled.Circle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        for (session in 1..4-uiState.completedSessions) {
                            Icon(
                                imageVector = MaterialSymbols.OutlinedFilled.Circle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primaryContainer
                            )
                        }
                    }
                    Text(
                        text = "Then 5 min short break",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = reset,
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Outlined.Restart_alt,
                        contentDescription = "Reset timer button",
                    )
                }
                Button(
                    onClick = toggleTimer,
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    if (uiState.isRunning) {
                        Icon(
                            imageVector = MaterialSymbols.OutlinedFilled.Square,
                            contentDescription = "pause button",
                        )
                    } else {
                        Icon(
                            imageVector = MaterialSymbols.Outlined.Play_arrow,
                            contentDescription = "play button",
                        )
                    }
                }
                Button(
                    onClick = nextPhase,
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Outlined.Skip_next,
                        contentDescription = "Skip timer button",
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

}

@Preview(showBackground = true, widthDp = 414, heightDp = 892)
@Composable
private fun ProjectDetailContentPreview() {
    JuggleTheme {
        PomodoroScreenContent(
            uiState = PomodoroUiState()
        )
    }
}