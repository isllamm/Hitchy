package com.hitchy.core.platform

interface ImageCompressor {
    suspend fun toJpeg(bytes: ByteArray, maxSidePx: Int = 1600, quality: Int = 80): ByteArray
}
