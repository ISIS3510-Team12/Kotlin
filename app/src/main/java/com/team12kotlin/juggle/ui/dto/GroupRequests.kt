package com.team12kotlin.juggle.ui.dto

import kotlinx.serialization.Serializable

@Serializable
data class GroupCreateRequest(
    val name: String,
    val description: String
)

@Serializable
data class GroupUpdateRequest(
    val name: String? = null,
    val description: String? = null
)

@Serializable
data class GroupMemberAddRequest(
    val email: String
)
