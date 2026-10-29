package com.hitchy.feature.debug

import com.hitchy.feature.debug.domain.DebugInfo

class FakeDebugInfo(
    var fcmToken: String? = "fake-fcm-token",
    var appCheckStatus: String = "OK",
    var remoteConfigValues: Map<String, String> = mapOf("minVersionCode" to "1"),
) : DebugInfo {

    var testCrashTriggered = false

    override suspend fun getFcmToken(): String? = fcmToken

    override suspend fun getAppCheckStatus(): String = appCheckStatus

    override suspend fun getRemoteConfigValues(): Map<String, String> = remoteConfigValues

    override fun triggerTestCrash() {
        testCrashTriggered = true
        throw RuntimeException("Fake crash")
    }
}
