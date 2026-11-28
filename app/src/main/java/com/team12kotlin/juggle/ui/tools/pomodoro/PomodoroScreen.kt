package com.team12kotlin.juggle.ui.tools.pomodoro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Play_arrow
import com.composables.icons.materialsymbols.outlined.Replay
import com.composables.icons.materialsymbols.outlined.Skip_next
import com.composables.icons.materialsymbols.outlinedfilled.Add
import com.composables.icons.materialsymbols.outlinedfilled.Circle
import com.composables.icons.materialsymbols.outlinedfilled.Remove
import com.composables.icons.materialsymbols.outlinedfilled.Square
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.ui.components.BackTopBar
import com.team12kotlin.juggle.ui.telemetry.ScreenName
import com.team12kotlin.juggle.ui.telemetry.TrackScreenLoad
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@Composable
fun PomodoroScreen(
    modifier: Modifier = Modifier,
    viewModel: PomodoroViewModel = viewModel(),
    onBackClick: () -> Unit = {}
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrackScreenLoad(ScreenName.PomodoroTimer)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val context = LocalContext.current
    val settings = Dependencies.pomodoroSettingsStore

    val onToggle = {
        val needsPermission = Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED

        if (needsPermission && !settings.notificationPermissionAsked) {
            settings.notificationPermissionAsked = true
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        viewModel.toggleTimer()
    }

    PomodoroScreenContent(
        uiState=uiState,
        modifier = modifier,
        toggleTimer = onToggle,
        reset = viewModel::reset,
        nextPhase = viewModel::onNextPhase,
        onUpdateFocusMinutes = viewModel::updateFocusMinutes,
        onUpdateBreakMinutes = viewModel::updateBreakMinutes,
        onBackClick = onBackClick
    )
}

@Composable
internal fun PomodoroScreenContent(
    uiState: PomodoroUiState,
    modifier: Modifier = Modifier,
    toggleTimer: () -> Unit = {},
    reset: () -> Unit = {},
    nextPhase: () -> Unit = {},
    onUpdateFocusMinutes: (Int) -> Unit = {},
    onUpdateBreakMinutes: (Int) -> Unit = {},
    onBackClick: () -> Unit = {}
){
    val nextText = when {
        uiState.phase == PomodoroPhase.FINISHED -> "Session complete!"
        uiState.nextPhase == PomodoroPhase.FOCUS -> "Up next: ${uiState.nextPhaseMinutes} min focus"
        uiState.nextPhase == PomodoroPhase.BREAK -> "Up next: ${uiState.nextPhaseMinutes} min break"
        else -> "Up next: you're done!"
    }

    val mainColor = when (uiState.phase) {
        PomodoroPhase.FOCUS -> MaterialTheme.colorScheme.primary
        PomodoroPhase.BREAK -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }

    val mainContainerColor = when (uiState.phase) {
        PomodoroPhase.FOCUS -> MaterialTheme.colorScheme.primaryContainer
        PomodoroPhase.BREAK -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }

    val mainOnContainerColor = when (uiState.phase) {
        PomodoroPhase.FOCUS -> MaterialTheme.colorScheme.onPrimaryContainer
        PomodoroPhase.BREAK -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    val title = when (uiState.phase) {
        PomodoroPhase.FOCUS -> "Study Session"
        PomodoroPhase.BREAK -> "Break Time"
        else -> "All Cycles Completed"
    }

    val subtitle = when (uiState.phase) {
        PomodoroPhase.FOCUS -> "Stay focused on your task"
        PomodoroPhase.BREAK -> "You're on a break. Rest and recharge"
        else -> "Great work!, Press play to start a new round"
    }

    Scaffold(
        modifier = modifier,
        topBar = { BackTopBar(title = "Pomodoro Timer", onBack = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = mainColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { uiState.progress },
                    modifier = Modifier.size(270.dp),
                    strokeWidth = 15.dp,
                    color = mainContainerColor,
                    trackColor = mainColor
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "%02d:%02d".format(uiState.remainingSeconds / 60, uiState.remainingSeconds % 60),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = mainColor
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (session in 1..uiState.completedSessions) {
                            Icon(
                                imageVector = MaterialSymbols.OutlinedFilled.Circle,
                                contentDescription = null,
                                tint = mainColor
                            )
                        }
                        for (session in 1..4-uiState.completedSessions) {
                            Icon(
                                imageVector = MaterialSymbols.OutlinedFilled.Circle,
                                contentDescription = null,
                                tint = mainContainerColor
                            )
                        }
                    }
                    Text(
                        text = nextText,
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
                        containerColor = mainContainerColor,
                        contentColor = mainOnContainerColor
                    )
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Outlined.Replay,
                        contentDescription = "Reset timer button",
                    )
                }
                Button(
                    onClick = toggleTimer,
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = mainOnContainerColor,
                        contentColor = mainContainerColor
                    )
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
                        containerColor = mainContainerColor,
                        contentColor = mainOnContainerColor
                    )
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Outlined.Skip_next,
                        contentDescription = "Skip timer button",
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
            ) {
                SettingsSection(
                    uiState= uiState,
                    mainColor= mainColor,
                    mainContainerColor= mainContainerColor,
                    mainOnContainerColor= mainOnContainerColor,
                    onFocus = onUpdateFocusMinutes,
                    onBreak = onUpdateBreakMinutes
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    uiState: PomodoroUiState,
    mainColor: Color,
    mainContainerColor: Color,
    mainOnContainerColor: Color,
    onFocus: (Int) -> Unit,
    onBreak: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .widthIn(max = 480.dp)
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            contentColor = mainColor,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(Modifier.padding(vertical = 4.dp)) {
            PomodoroSettingsCard(
                phase = "Focus",
                minuteValue = uiState.focusMinutes,
                mainContainerColor= mainContainerColor,
                mainOnContainerColor = mainOnContainerColor,
                onChange = onFocus
            )
            PomodoroSettingsCard(
                phase = "Break",
                minuteValue = uiState.breakMinutes,
                mainContainerColor = mainContainerColor,
                mainOnContainerColor = mainOnContainerColor,
                onChange = onBreak
            )
        }
    }
}

@Composable
fun PomodoroSettingsCard(
    phase: String,
    minuteValue: Int,
    mainContainerColor: Color,
    mainOnContainerColor: Color,
    onChange: (Int)-> Unit = {},
) {
    Row(
        modifier = Modifier.padding(8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = phase,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, end = 64.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onChange(if (minuteValue > 0) minuteValue - 1 else 0) },
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = mainContainerColor,
                    contentColor = mainOnContainerColor
                )
            ) {
                Icon(
                    imageVector = MaterialSymbols.OutlinedFilled.Remove,
                    contentDescription = "Skip timer button",
                )
            }
            Text(
                text = minuteValue.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { onChange(if(minuteValue < 99) minuteValue + 1 else 99) },
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = mainContainerColor,
                    contentColor = mainOnContainerColor
                )
            ) {
                Icon(
                    imageVector = MaterialSymbols.OutlinedFilled.Add,
                    contentDescription = "Skip timer button",
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 414, heightDp = 892)
@Composable
private fun PomodoroFocusContentPreview() {
    JuggleTheme {
        PomodoroScreenContent(
            uiState = PomodoroUiState(
                phase = PomodoroPhase.FOCUS,
                remainingSeconds = 25*60
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 414, heightDp = 892)
@Composable
private fun PomodoroBreakContentPreview() {
    JuggleTheme {
        PomodoroScreenContent(
            uiState = PomodoroUiState(
                phase = PomodoroPhase.BREAK,
                remainingSeconds = 5*60,
                totalSeconds = 5*60
            )
        )
    }
}


@Preview(showBackground = true, widthDp = 414, heightDp = 892)
@Composable
private fun PomodoroFinishedContentPreview() {
    JuggleTheme {
        PomodoroScreenContent(
            uiState = PomodoroUiState(
                phase = PomodoroPhase.FINISHED,
                remainingSeconds = 0,
                totalSeconds = 1
            )
        )
    }
}