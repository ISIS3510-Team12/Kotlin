package com.team12kotlin.juggle.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import com.team12kotlin.juggle.data.remote.JuggleApi
import com.team12kotlin.juggle.ui.dto.ProjectPhoto
import com.team12kotlin.juggle.utils.decodeSampledBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException

private const val UPLOAD_MAX_PX = 2048
private const val UPLOAD_QUALITY = 85
private const val THUMBNAIL_MAX_PX = 720
private const val THUMBNAIL_QUALITY = 80
private val TEXT = "text/plain".toMediaType()

class ProjectPhotoRepository(
    private val api: JuggleApi,
    private val context: Context
) {
    suspend fun getPhotos(projectId: Int): List<ProjectPhoto> = api.getProjectPhotos(projectId)

    suspend fun download(projectId: Int, photoId: Int, thumbnail: Boolean = false): ByteArray =
        withContext(Dispatchers.IO) {
            api.getProjectPhotoFile(projectId, photoId, if (thumbnail) "thumb" else "full").bytes()
        }

    // Compresses off the main thread, then uploads on Dispatchers.IO.
    suspend fun upload(projectId: Int, uri: Uri, clientUploadId: String, caption: String?): ProjectPhoto {
        val photo = compress(uri, UPLOAD_MAX_PX, UPLOAD_QUALITY)
        val thumbnail = compress(uri, THUMBNAIL_MAX_PX, THUMBNAIL_QUALITY)
        return withContext(Dispatchers.IO) {
            api.uploadProjectPhoto(
                projectId = projectId,
                file = jpegPart("file", photo),
                thumbnail = jpegPart("thumbnail", thumbnail),
                clientUploadId = clientUploadId.toRequestBody(TEXT),
                caption = caption?.toRequestBody(TEXT)
            )
        }
    }

    suspend fun delete(projectId: Int, photoId: Int) = api.deleteProjectPhoto(projectId, photoId)

    private fun jpegPart(name: String, bytes: ByteArray) = MultipartBody.Part.createFormData(
        name,
        "$name.jpg",
        bytes.toRequestBody("image/jpeg".toMediaType())
    )

    // Shrinks the photo so it fits the server's size limit.
    private suspend fun compress(uri: Uri, maxPx: Int, quality: Int): ByteArray {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        val bitmap = decodeSampledBitmap(source, maxPx, software = true)
            ?: throw IOException("Could not read the photo")
        return withContext(Dispatchers.Default) {
            ByteArrayOutputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
                output.toByteArray()
            }
        }
    }
}
