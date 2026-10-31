package com.team12kotlin.juggle.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun ShakeEffect(
    enabled: Boolean = true,
    onShake: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnShake by rememberUpdatedState(onShake)

    DisposableEffect(enabled, lifecycleOwner) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (!enabled || accelerometer == null) {
            return@DisposableEffect onDispose {  }
        }

        val shakeDetector = ShakeDetector { currentOnShake() }

        val observer = LifecycleEventObserver {_, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> sensorManager.registerListener(
                    shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI
                )

                Lifecycle.Event.ON_PAUSE -> sensorManager.unregisterListener(shakeDetector)
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            sensorManager.unregisterListener(shakeDetector)
        }
    }
}