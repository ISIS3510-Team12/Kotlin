package com.team12kotlin.juggle.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "cache_metadata",
    primaryKeys = ["userId", "cachedResource", "scopeId"]
)
data class CacheMetadataEntity(
    val userId: String,
    val cachedResource: String,
    val scopeId: String,
    val cachedAt: Long,
)
