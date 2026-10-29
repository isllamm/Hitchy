package com.hitchy.core.platform

class FakeImageCompressor : ImageCompressor {
    var lastCompressedBytes: ByteArray? = null
    var returnBytes: ByteArray? = null

    override suspend fun toJpeg(bytes: ByteArray, maxSidePx: Int, quality: Int): ByteArray {
        lastCompressedBytes = bytes
        return returnBytes ?: bytes
    }
}
