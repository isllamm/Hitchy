package com.hitchy.core.platform

/** Turns a profile picture path ("avatars/{uid}.jpg") into a URL an image loader can fetch. */
interface PhotoUrlResolver {
    /** The download URL of [photoPath], or null when the file is missing or cannot be read. */
    suspend fun urlFor(photoPath: String): String?
}
