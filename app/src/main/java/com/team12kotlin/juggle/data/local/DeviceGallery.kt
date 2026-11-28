package com.team12kotlin.juggle.data.local

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

// A photo stored in the phone's gallery.
data class DevicePhoto(val uri: Uri, val addedAtSeconds: Long)

// Queries and writes the phone's gallery through MediaStore.
class DeviceGallery(private val context: Context) {

    private val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    // Newest photos on the phone. Only returns what the photo permission allows.
    suspend fun recentPhotos(limit: Int = 90): List<DevicePhoto> = query(null, null, limit)

    // Photos this app saved for a project. Needs no permission.
    suspend fun savedPhotos(projectId: Int): List<DevicePhoto> = query(
        "${MediaStore.Images.Media.RELATIVE_PATH} = ?",
        arrayOf("${folder(projectId)}/"),
        Int.MAX_VALUE
    )

    suspend fun save(projectId: Int, fileName: String, bytes: ByteArray) = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, folder(projectId))
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(collection, values) ?: throw IOException("Could not create the photo")
        try {
            resolver.openOutputStream(uri)?.use { it.write(bytes) }
                ?: throw IOException("Could not write the photo")
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }
    }

    private fun folder(projectId: Int) = "${Environment.DIRECTORY_PICTURES}/Juggle/project-$projectId"

    private suspend fun query(
        selection: String?,
        args: Array<String>?,
        limit: Int
    ): List<DevicePhoto> = withContext(Dispatchers.IO) {
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED)
        context.contentResolver.query(
            collection,
            projection,
            selection,
            args,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            buildList {
                while (size < limit && cursor.moveToNext()) {
                    val uri = ContentUris.withAppendedId(collection, cursor.getLong(0))
                    add(DevicePhoto(uri, cursor.getLong(1)))
                }
            }
        }.orEmpty()
    }
}
