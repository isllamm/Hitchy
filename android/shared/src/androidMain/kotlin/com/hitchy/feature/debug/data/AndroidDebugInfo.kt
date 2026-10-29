package com.hitchy.feature.debug.data

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.hitchy.core.model.AppConfig
import com.hitchy.feature.debug.domain.DebugInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AndroidDebugInfo(
    private val context: Context,
    private val appConfig: AppConfig,
) : DebugInfo {

    override suspend fun getFcmToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.awaitTask()
        } catch (e: Exception) {
            "Error: ${e.message ?: "Failed to get token"}"
        }
    }

    override suspend fun getAppCheckStatus(): String {
        if (appConfig.useEmulator) {
            return "not installed (emulator)"
        }
        return try {
            FirebaseAppCheck.getInstance().getAppCheckToken(false).awaitTask()
            "OK"
        } catch (e: Exception) {
            e.message ?: "App Check error"
        }
    }

    override suspend fun getRemoteConfigValues(): Map<String, String> {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val defaults: Map<String, Any> = mapOf(
            "minVersionCode" to 1L,
            "scooterEnabled" to false,
            "assistantEnabled" to false,
            "fixedRideEnabled" to false,
            "rewardsEnabled" to false,
        )

        try {
            remoteConfig.setDefaultsAsync(defaults).awaitTask()
        } catch (_: Exception) {
        }

        try {
            remoteConfig.fetchAndActivate().awaitTask()
        } catch (_: Exception) {
            // In-app defaults are used if fetch fails
        }

        val keys = listOf(
            "minVersionCode",
            "scooterEnabled",
            "assistantEnabled",
            "fixedRideEnabled",
            "rewardsEnabled",
        )

        return keys.associateWith { key ->
            val value = remoteConfig.getString(key)
            if (value.isNotEmpty()) value else defaults[key]?.toString().orEmpty()
        }
    }

    override fun triggerTestCrash() {
        throw RuntimeException("Test crash from Hitchy debug screen")
    }

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { res -> cont.resume(res) }
        addOnFailureListener { err -> cont.resumeWithException(err) }
        addOnCanceledListener { cont.cancel() }
    }
}
