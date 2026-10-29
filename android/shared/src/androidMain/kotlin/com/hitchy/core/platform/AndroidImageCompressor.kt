package com.hitchy.core.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

private const val ROTATE_90_DEG = 90f
private const val ROTATE_180_DEG = 180f
private const val ROTATE_270_DEG = 270f
private const val MAX_JPEG_QUALITY = 100

class AndroidImageCompressor : ImageCompressor {

    override suspend fun toJpeg(
        bytes: ByteArray,
        maxSidePx: Int,
        quality: Int,
    ): ByteArray = withContext(Dispatchers.Default) {
        if (bytes.isEmpty()) return@withContext bytes

        val orientation = try {
            val exif = ExifInterface(ByteArrayInputStream(bytes))
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } catch (_: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

        val origWidth = options.outWidth
        val origHeight = options.outHeight
        if (origWidth <= 0 || origHeight <= 0) {
            return@withContext bytes
        }

        val longestSide = max(origWidth, origHeight)
        var sampleSize = 1
        while (longestSide / (sampleSize * 2) >= maxSidePx) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val decodedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
            ?: return@withContext bytes

        val currentLongest = max(decodedBitmap.width, decodedBitmap.height)
        val scaledBitmap = if (currentLongest > maxSidePx) {
            val scale = maxSidePx.toFloat() / currentLongest.toFloat()
            val targetW = (decodedBitmap.width * scale).roundToInt()
            val targetH = (decodedBitmap.height * scale).roundToInt()
            Bitmap.createScaledBitmap(decodedBitmap, targetW, targetH, true)
        } else {
            decodedBitmap
        }

        val matrix = createExifMatrix(orientation)
        val rotatedBitmap = if (!matrix.isIdentity) {
            Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
        } else {
            scaledBitmap
        }

        val outputStream = ByteArrayOutputStream()
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(0, MAX_JPEG_QUALITY), outputStream)
        outputStream.toByteArray()
    }

    private fun createExifMatrix(orientation: Int): Matrix = Matrix().apply {
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(ROTATE_90_DEG)
            ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(ROTATE_180_DEG)
            ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(ROTATE_270_DEG)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)
        }
    }
}
