package com.team12kotlin.juggle.ui.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectCreateRequest(
    val name: String,
    val description: String = "",
    val deadline: String,
    @SerialName("group_id")
    val groupId: Int
)

@Serializable
data class ProjectUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val deadline: String? = null,
    @SerialName("group_id")
    val groupId: Int? = null
)
