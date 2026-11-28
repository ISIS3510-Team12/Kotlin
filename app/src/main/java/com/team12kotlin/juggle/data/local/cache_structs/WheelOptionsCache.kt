package com.team12kotlin.juggle.data.local.cache_structs

import android.util.SparseArray
import com.team12kotlin.juggle.ui.dto.WheelOption

/** Options of each wheel in memory; SparseArray is not thread-safe, so every access is locked. */
class WheelOptionsCache {
    private val options = SparseArray<List<WheelOption>>()

    fun get(wheelId: Int): List<WheelOption>? = synchronized(options) { options[wheelId] }

    fun put(wheelId: Int, value: List<WheelOption>) {
        synchronized(options) { options.put(wheelId, value) }
    }

    fun remove(wheelId: Int) {
        synchronized(options) { options.remove(wheelId) }
    }
}
