package com.team12kotlin.juggle.ui.telemetry

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.team12kotlin.juggle.data.telemetry.ScreenLoadReporter

@Composable
fun TrackScreenLoad(screen: ScreenName, isLoading: Boolean) {
    var startedAt by remember(screen) { mutableStateOf<Long?>(null) }
    var reported by remember(screen) { mutableStateOf(false) }

    LaunchedEffect(screen, isLoading) {
        if (isLoading) {
            if (startedAt == null) {
                startedAt = SystemClock.elapsedRealtime()
            }
        } else if (!reported && startedAt != null) {
            ScreenLoadReporter.report(screen.value, SystemClock.elapsedRealtime() - startedAt!!)
            reported = true
        }
    }
}

@Composable
fun TrackScreenLoad(screen: ScreenName) {
    val startedAt = remember(screen) { SystemClock.elapsedRealtime() }
    LaunchedEffect(screen) {
        ScreenLoadReporter.report(screen.value, SystemClock.elapsedRealtime() - startedAt)
    }
}
