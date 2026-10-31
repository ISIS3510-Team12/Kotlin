package com.team12kotlin.juggle.ui.connectivity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team12kotlin.juggle.data.connectivity.ConnectivityObserver
import kotlinx.coroutines.flow.map

private val LocalIsOnline = staticCompositionLocalOf<Boolean?> { null }

@Composable
fun IsOnlineProvider(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val observer = remember(context) {
        ConnectivityObserver(context.applicationContext)
    }
    val isOnline by remember(observer) {
        observer.isOnline.map<Boolean, Boolean?> { it }
    }.collectAsStateWithLifecycle(initialValue = null)

    CompositionLocalProvider(LocalIsOnline provides isOnline, content = content)
}

@Composable
fun rememberIsOnline(): Boolean? = LocalIsOnline.current
