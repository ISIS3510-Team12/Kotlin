package com.team12kotlin.juggle.data.repository

import com.team12kotlin.juggle.data.local.cache_structs.WheelOptionsCache
import com.team12kotlin.juggle.data.local.dao.WheelDao
import com.team12kotlin.juggle.data.local.entity.SpinHistoryEntity
import com.team12kotlin.juggle.data.local.entity.WheelEntity
import com.team12kotlin.juggle.data.local.entity.WheelOptionEntity
import com.team12kotlin.juggle.ui.dto.MAX_WHEEL_OPTIONS
import com.team12kotlin.juggle.ui.dto.MIN_WHEEL_OPTIONS
import com.team12kotlin.juggle.ui.dto.Wheel
import com.team12kotlin.juggle.ui.dto.WheelOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Wheels only live on the device, so there is no network and no stale data to handle. */
class WheelRepository(
    private val wheelDao: WheelDao,
    private val optionsCache: WheelOptionsCache,
    private val userId: String
) {
    fun wheels(): Flow<List<Wheel>> =
        wheelDao.observeWheels(userId).map { wheels ->
            wheels.map { Wheel(it.wheel.wheelId, it.wheel.name, it.optionCount) }
        }

    suspend fun getWheel(wheelId: Int): Wheel? =
        wheelDao.getWheel(userId, wheelId)?.let { Wheel(it.wheelId, it.name) }

    suspend fun createWheel(name: String, options: List<String>): Int {
        val cleanName = requireName(name)
        val cleanOptions = options.map(::requireOption)
        require(cleanOptions.size >= MIN_WHEEL_OPTIONS) { "Add at least two options." }
        require(cleanOptions.size <= MAX_WHEEL_OPTIONS) { MAX_OPTIONS_MESSAGE }
        return wheelDao.createWheel(
            WheelEntity(userId = userId, name = cleanName, createdAt = System.currentTimeMillis()),
            cleanOptions
        )
    }

    suspend fun renameWheel(wheelId: Int, name: String) {
        wheelDao.renameWheel(wheelId, requireName(name))
    }

    suspend fun deleteWheel(wheelId: Int) {
        wheelDao.deleteWheel(wheelId)
        optionsCache.remove(wheelId)
    }

    suspend fun getOptions(wheelId: Int): List<WheelOption> =
        optionsCache.get(wheelId) ?: wheelDao.getOptions(wheelId)
            .map { it.toOption() }
            .also { optionsCache.put(wheelId, it) }

    suspend fun addOption(wheelId: Int, text: String) {
        val cleanText = requireOption(text)
        require(getOptions(wheelId).size < MAX_WHEEL_OPTIONS) { MAX_OPTIONS_MESSAGE }
        wheelDao.addOption(wheelId, cleanText)
        optionsCache.remove(wheelId)
    }

    suspend fun editOption(wheelId: Int, optionId: Int, text: String) {
        wheelDao.updateOption(optionId, requireOption(text))
        optionsCache.remove(wheelId)
    }

    suspend fun deleteOption(wheelId: Int, optionId: Int) {
        wheelDao.deleteOption(optionId)
        optionsCache.remove(wheelId)
    }

    suspend fun recordSpin(wheelId: Int, selectedText: String) {
        wheelDao.insertSpin(
            SpinHistoryEntity(
                wheelId = wheelId,
                selectedOptionText = selectedText,
                spunAt = System.currentTimeMillis()
            )
        )
    }

    private fun requireName(name: String): String =
        name.trim().also { require(it.isNotEmpty()) { "Enter a wheel name." } }

    private fun requireOption(text: String): String =
        text.trim().also { require(it.isNotEmpty()) { "An option can't be empty." } }

    private fun WheelOptionEntity.toOption() = WheelOption(optionId, optionText)

    private companion object {
        const val MAX_OPTIONS_MESSAGE = "A wheel can have up to $MAX_WHEEL_OPTIONS options."
    }
}
