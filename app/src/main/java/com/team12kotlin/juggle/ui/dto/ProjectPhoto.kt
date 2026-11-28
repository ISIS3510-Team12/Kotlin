package com.team12kotlin.juggle.ui.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectPhoto(
    val id: Int,
    @SerialName("project_id") val projectId: Int,
    val caption: String? = null,
    @SerialName("uploader_id") val uploaderId: String = "",
    @SerialName("uploader_name") val uploaderName: String = "",
    @SerialName("created_at") val createdAt: String = ""
)
