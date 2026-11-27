package com.team12kotlin.juggle.ui.projects.photos

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.ui.dto.ProjectPhoto
import com.team12kotlin.juggle.utils.decodeSampledBitmap
import kotlinx.coroutines.CancellationException
import java.nio.ByteBuffer

const val THUMBNAIL_PX = 720
const val FULL_PX = 2048

// Where the pixels of a photo come from, the server or the phone.
sealed interface PhotoSource {
    val key: String

    data class Remote(val photo: ProjectPhoto) : PhotoSource {
        override val key = "remote:${photo.id}"
    }

    data class Local(val uri: Uri) : PhotoSource {
        override val key = uri.toString()
    }
}

private const val MAX_CACHED_THUMBNAILS = 40
private const val MAX_CACHED_FULL_PHOTOS = 4

// LRU of decoded bitmaps. Full size photos are much heavier, so they get their own smaller cache.
private val thumbnailCache = LruCache<String, Bitmap>(MAX_CACHED_THUMBNAILS)
private val fullPhotoCache = LruCache<String, Bitmap>(MAX_CACHED_FULL_PHOTOS)

@Composable
fun PhotoImage(
    source: PhotoSource,
    modifier: Modifier = Modifier,
    maxPx: Int = THUMBNAIL_PX,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null
) {
    val context = LocalContext.current
    val cache = if (maxPx <= THUMBNAIL_PX) thumbnailCache else fullPhotoCache
    val cacheKey = "${source.key}@$maxPx"
    val bitmap by produceState<Bitmap?>(initialValue = cache.get(cacheKey), cacheKey) {
        value = cache.get(cacheKey) ?: try {
            val imageSource = when (source) {
                is PhotoSource.Remote -> ImageDecoder.createSource(
                    ByteBuffer.wrap(
                        Dependencies.projectPhotoRepository.download(
                            source.photo.projectId,
                            source.photo.id,
                            thumbnail = maxPx <= THUMBNAIL_PX
                        )
                    )
                )
                is PhotoSource.Local -> ImageDecoder.createSource(context.contentResolver, source.uri)
            }
            decodeSampledBitmap(imageSource, maxPx)
                ?.also { cache.put(cacheKey, it) }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    val image = bitmap
    if (image == null) {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh))
    } else {
        Image(
            bitmap = image.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}
