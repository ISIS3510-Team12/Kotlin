package com.team12kotlin.juggle.data.local

import androidx.room.withTransaction
import com.team12kotlin.juggle.data.local.dao.CacheMetadataDao
import com.team12kotlin.juggle.data.local.entity.CacheMetadataEntity
import okio.IOException

sealed interface CacheResult<out T> {
    val data: T

    val isCacheMiss: Boolean
        get() = this is Stale && lastSyncedAt == null
    data class Fresh<T>(override val data: T): CacheResult<T>
    data class Stale<T>(override val data: T, val lastSyncedAt: Long?) : CacheResult<T>
}

class NetworkFirstCache(
    private val db: AppDatabase,
    private val metadataDao: CacheMetadataDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    suspend fun <T> fetch(
        userId: String,
        resource: String,
        scopeId: String,
        remote: suspend () -> T,
        saveLocal: suspend (T) -> Unit,
        readLocal: suspend () -> T,
    ): CacheResult<T> {
        val remoteData = try {
            remote()
        } catch (e: IOException) {
            return CacheResult.Stale(
                data = readLocal(),
                lastSyncedAt = metadataDao.lastSyncedAt(userId, resource, scopeId)
            )
        }
        db.withTransaction {
            saveLocal(remoteData)
            metadataDao.insertOne(CacheMetadataEntity(userId, resource, scopeId, clock()))
        }
        LastSync.mark(clock())
        return CacheResult.Fresh(remoteData)
    }
}