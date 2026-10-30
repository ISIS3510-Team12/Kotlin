package com.team12kotlin.juggle.ui.connectivity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team12kotlin.juggle.data.connectivity.ConnectivityObserver
import kotlinx.coroutines.flow.map

@Composable
fun rememberIsOnline(): Boolean? {
    val context = LocalContext.current
    val observer = remember(context) {
        ConnectivityObserver(context.applicationContext)
    }
    return observer.isOnline
        .map<Boolean, Boolean?> { it }
        .collectAsStateWithLifecycle(initialValue = null)
        .value
}
