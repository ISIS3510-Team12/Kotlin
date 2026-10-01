package com.team12kotlin.juggle.ui.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: Int = 0,
    val name: String,
    val description: String = "",
    val deadline: String? = null,
    @SerialName("group_id")
    val groupId: Int = 0
)
