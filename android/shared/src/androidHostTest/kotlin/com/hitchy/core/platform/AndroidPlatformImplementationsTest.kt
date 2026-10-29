package com.hitchy.core.platform

import android.app.Activity
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AndroidPlatformImplementationsTest {

    @Test
    fun testAndroidActivityProvider() {
        val provider = AndroidActivityProvider()
        assertNull(provider.currentActivity)

        val activity = Activity()
        provider.onActivityCreated(activity, null)
        assertEquals(activity, provider.currentActivity)

        provider.onActivityResumed(activity)
        assertEquals(activity, provider.currentActivity)

        provider.onActivityDestroyed(activity)
        assertNull(provider.currentActivity)
    }

    @Test
    fun testNoOpAnalytics() {
        val analytics = NoOpAnalytics()
        // Should execute cleanly without error
        analytics.log("test_event", mapOf("key" to "value"))
    }

    @Test
    fun testAndroidImageCompressorEmptyBytes() = runTest {
        val compressor = AndroidImageCompressor()
        val emptyResult = compressor.toJpeg(byteArrayOf())
        assertEquals(0, emptyResult.size)
    }
}
