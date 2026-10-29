package com.hitchy.core.platform

import com.hitchy.core.data.AppError
import com.hitchy.core.data.AppResult
import com.hitchy.core.model.GeoPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlatformInterfacesTest {

    @Test
    fun testFakePhoneAuth() = runTest {
        val phoneAuth = FakePhoneAuth()
        phoneAuth.start("+201001234567")
        assertEquals("+201001234567", phoneAuth.lastStartedPhone)

        phoneAuth.emitEvent(PhoneAuthEvent.CodeSent(canResendAtMs = 60000L))
        val event = phoneAuth.events.first()
        assertTrue(event is PhoneAuthEvent.CodeSent)
        assertEquals(60000L, event.canResendAtMs)

        val result = phoneAuth.confirm("123456")
        assertTrue(result is AppResult.Ok)

        phoneAuth.confirmResult = AppResult.Err(AppError.Reason("OTP_INVALID"))
        val failResult = phoneAuth.confirm("000000")
        assertTrue(failResult is AppResult.Err)
        assertEquals("OTP_INVALID", (failResult.error as AppError.Reason).code)
    }

    @Test
    fun testFakeLocationProvider() = runTest {
        val provider = FakeLocationProvider(permissionGranted = false)
        assertFalse(provider.hasPermission())
        assertNull(provider.current())

        provider.permissionGranted = true
        assertTrue(provider.hasPermission())
        val loc = provider.current()
        assertEquals(GeoPoint(lat = 30.0444, lng = 31.2357), loc)
    }

    @Test
    fun testFakeTripTracker() {
        val tracker = FakeTripTracker()
        assertFalse(tracker.isTracking)
        assertNull(tracker.activeRideId)

        tracker.start("ride_123")
        assertTrue(tracker.isTracking)
        assertEquals("ride_123", tracker.activeRideId)

        tracker.stop()
        assertFalse(tracker.isTracking)
        assertNull(tracker.activeRideId)
    }

    @Test
    fun testFakeImageCompressor() = runTest {
        val compressor = FakeImageCompressor()
        val dummyBytes = byteArrayOf(1, 2, 3)
        val result = compressor.toJpeg(dummyBytes, maxSidePx = 800, quality = 70)
        assertEquals(dummyBytes, result)
        assertEquals(dummyBytes, compressor.lastCompressedBytes)
    }

    @Test
    fun testFakeSharer() {
        val sharer = FakeSharer()
        sharer.shareText("Join my Hitchy ride!")
        assertEquals(listOf("Join my Hitchy ride!"), sharer.sharedTexts)
    }

    @Test
    fun testFakeDialer() {
        val dialer = FakeDialer()
        dialer.dial("+201001234567")
        assertEquals("+201001234567", dialer.dialedNumber)

        dialer.sms("+201001234567", "I am waiting at the pickup point")
        assertEquals("+201001234567", dialer.smsNumber)
        assertEquals("I am waiting at the pickup point", dialer.smsBody)
    }

    @Test
    fun testFakeAnalytics() {
        val analytics = FakeAnalytics()
        analytics.log("ride_booked", mapOf("rideId" to "r_1"))
        assertEquals(1, analytics.loggedEvents.size)
        assertEquals("ride_booked", analytics.loggedEvents[0].name)
        assertEquals("r_1", analytics.loggedEvents[0].params["rideId"])
    }
}
