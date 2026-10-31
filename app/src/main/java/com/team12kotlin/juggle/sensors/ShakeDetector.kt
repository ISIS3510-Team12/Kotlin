package com.team12kotlin.juggle.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import kotlin.math.sqrt

class ShakeDetector(
    private val shakeThreshold: Float = 2.7f,
    private val requiredShakes: Int = 2,
    private val shakeWindowMs: Long = 800L,
    private val minShakeGapMs: Long = 100L,
    private val cooldownMs: Long = 1000L,
    private val onShake: () -> Unit,
): SensorEventListener {
    private var firstShakeTime = 0L
    private var lastShakeTime = 0L
    private var lastTriggerTime = 0L
    private var shakeCount = 0

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val gX = event.values[0] / SensorManager.GRAVITY_EARTH
        val gY = event.values[1] / SensorManager.GRAVITY_EARTH
        val gZ = event.values[2] / SensorManager.GRAVITY_EARTH
        val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

        if (gForce <= shakeThreshold) return

        val now = SystemClock.elapsedRealtime()
        if (now - lastTriggerTime < cooldownMs) return // if it was triggered recently, ignore
        if (now - lastShakeTime < minShakeGapMs) return // if shakes are done to fast, ignore them

        lastShakeTime = now

        if (shakeCount == 0 || now - firstShakeTime > shakeWindowMs) {
            // init again if the last shake is too old
            firstShakeTime = now
            shakeCount = 0
        }

        shakeCount++

        if (shakeCount >= requiredShakes) {
            shakeCount = 0
            lastTriggerTime = now
            onShake()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) = Unit
}