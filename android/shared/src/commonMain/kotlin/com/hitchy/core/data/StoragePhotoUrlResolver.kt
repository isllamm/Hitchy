package com.hitchy.core.data

import com.hitchy.core.platform.PhotoUrlResolver
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.storage.storage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Resolves picture paths through Firebase Storage. The URLs are kept in memory for the life of the app. */
class StoragePhotoUrlResolver : PhotoUrlResolver {
    private val cache = mutableMapOf<String, String>()
    private val mutex = Mutex()

    @Suppress("TooGenericExceptionCaught")
    override suspend fun urlFor(photoPath: String): String? {
        mutex.withLock { cache[photoPath] }?.let { return it }
        val url = try {
            Firebase.storage.reference(photoPath).getDownloadUrl()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            return null
        }
        mutex.withLock { cache[photoPath] = url }
        return url
    }
}
