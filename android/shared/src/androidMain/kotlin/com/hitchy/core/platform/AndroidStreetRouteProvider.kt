package com.hitchy.core.platform

import com.hitchy.core.model.GeoPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Street paths from the free OSRM demo server (ADR 0003 row 171). It needs no key and costs nothing, but it is a
 * shared public server with fair-use limits: fine for the demo, to be replaced before launch. Only the points of the
 * route are sent, never a user id. Results are kept in memory, and any failure gives null (a straight line).
 */
class AndroidStreetRouteProvider : StreetRouteProvider {
    private val cache = mutableMapOf<String, List<GeoPoint>>()
    private val mutex = Mutex()

    @Suppress("TooGenericExceptionCaught")
    override suspend fun routeThrough(points: List<GeoPoint>): List<GeoPoint>? {
        if (points.size < MIN_POINTS || points.size > MAX_POINTS) return null
        val path = points.joinToString(";") { "${it.lng},${it.lat}" }
        mutex.withLock { cache[path] }?.let { return it }
        return try {
            val found = withContext(Dispatchers.IO) { fetch(path) } ?: return null
            mutex.withLock { cache[path] = found }
            found
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    private fun fetch(path: String): List<GeoPoint>? {
        val connection = URL("$BASE_URL$path?overview=full&geometries=geojson").openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.setRequestProperty("User-Agent", USER_AGENT)
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(body)
            if (root.optString("code") != "Ok") return null
            val coordinates = root.getJSONArray("routes").getJSONObject(0)
                .getJSONObject("geometry").getJSONArray("coordinates")
            return List(coordinates.length()) { i ->
                val pair = coordinates.getJSONArray(i)
                GeoPoint(lat = pair.getDouble(1), lng = pair.getDouble(0))
            }.takeIf { it.size >= MIN_POINTS }
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val BASE_URL = "https://router.project-osrm.org/route/v1/driving/"
        const val USER_AGENT = "HitchyDemo/0.1 (carpooling demo)"
        const val TIMEOUT_MS = 8_000
        const val MIN_POINTS = 2
        const val MAX_POINTS = 25
    }
}
