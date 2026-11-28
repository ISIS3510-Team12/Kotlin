package com.team12kotlin.juggle.utils

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Decodes a compressed version of a photo, so the whole image is not needed.
// ImageDecoder applies the EXIF orientation, so portrait photos are not shown sideways.
suspend fun decodeSampledBitmap(
    source: ImageDecoder.Source,
    maxPx: Int,
    software: Boolean = false
): Bitmap? =
    withContext(Dispatchers.Default) {
        runCatching {
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                if (software) decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.setTargetSampleSize(
                    sampleSize(info.size.width, info.size.height, maxPx)
                )
            }
        }.getOrNull()
    }

// How much to shrink the photo so it is not decoded at full resolution.
private fun sampleSize(width: Int, height: Int, maxPx: Int): Int {
    val longestSide = maxOf(width, height)
    var sample = 1
    while (longestSide / sample > maxPx) {
        sample *= 2
    }
    return sample
}
