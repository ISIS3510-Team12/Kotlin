package com.team12kotlin.juggle.ui.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val MAX_NOTIFY_WITHIN_METERS = 1000

@Serializable
data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    @SerialName("notify_within")
    val notifyWithin: Int
)

@Serializable
data class TaskTodaySummary(
    @SerialName("pending_count")
    val pendingCount: Int = 0,
    @SerialName("today_count")
    val todayCount: Int = 0,
    @SerialName("overdue_count")
    val overdueCount: Int = 0,
    val titles: List<String> = emptyList()
)
