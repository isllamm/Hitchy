package com.hitchy.feature.debug.domain

interface DebugInfo {
    suspend fun getFcmToken(): String?
    suspend fun getAppCheckStatus(): String
    suspend fun getRemoteConfigValues(): Map<String, String>
    fun triggerTestCrash()
}
