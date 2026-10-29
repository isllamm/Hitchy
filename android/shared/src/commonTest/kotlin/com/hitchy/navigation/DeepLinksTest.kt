package com.hitchy.navigation

import com.hitchy.core.model.Role
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeepLinksTest {

    // --- Valid hitchy:// links ---

    @Test
    fun parsesHomeLink() {
        val target = DeepLinks.parse("hitchy://home", Role.RIDER)
        assertEquals(DeepLinkTarget.Screen(Home), target)
    }

    @Test
    fun parsesNotificationsLink() {
        val target = DeepLinks.parse("hitchy://notifications", Role.RIDER)
        assertEquals(DeepLinkTarget.Screen(Notifications), target)
    }

    @Test
    fun parsesSearchLink() {
        val target = DeepLinks.parse("hitchy://search", Role.RIDER)
        assertEquals(DeepLinkTarget.Screen(Search), target)
    }

    @Test
    fun parsesTripsLink() {
        val target = DeepLinks.parse("hitchy://trips", Role.RIDER)
        assertEquals(DeepLinkTarget.Screen(MyTrips), target)
    }

    @Test
    fun parsesFeesLink() {
        val target = DeepLinks.parse("hitchy://fees", Role.RIDER)
        assertEquals(DeepLinkTarget.Screen(Fees), target)
    }

    @Test
    fun parsesPackagesLink() {
        val target = DeepLinks.parse("hitchy://packages", Role.HOST)
        assertEquals(DeepLinkTarget.Screen(Packages), target)
    }

    @Test
    fun parsesRewardsLink() {
        val target = DeepLinks.parse("hitchy://rewards", Role.RIDER)
        assertEquals(DeepLinkTarget.Screen(Rewards), target)
    }

    @Test
    fun parsesShopLink() {
        assertEquals(DeepLinkTarget.Screen(Shop), DeepLinks.parse("hitchy://shop", Role.RIDER))
        assertEquals(DeepLinkTarget.Screen(Shop), DeepLinks.parse("hitchy://shop", Role.HOST))
    }

    @Test
    fun parsesProfileLinkToHomeTab() {
        // profile -> Home with TODO(P4-A04) to open profile bottom tab
        assertEquals(DeepLinkTarget.Screen(Home), DeepLinks.parse("hitchy://profile", Role.RIDER))
        assertEquals(DeepLinkTarget.Screen(Home), DeepLinks.parse("hitchy://profile", Role.HOST))
    }

    @Test
    fun parsesRideLinkByRole() {
        val hostTarget = DeepLinks.parse("hitchy://ride/ride-123", Role.HOST)
        assertEquals(DeepLinkTarget.Screen(HostRideDetails("ride-123")), hostTarget)

        val riderTarget = DeepLinks.parse("hitchy://ride/ride-123", Role.RIDER)
        assertEquals(DeepLinkTarget.Screen(RiderRideDetails("ride-123")), riderTarget)
    }

    @Test
    fun parsesTripLinkByRole() {
        val hostTarget = DeepLinks.parse("hitchy://trip/trip-456", Role.HOST)
        assertEquals(DeepLinkTarget.Screen(HostTrip("trip-456")), hostTarget)

        val riderTarget = DeepLinks.parse("hitchy://trip/trip-456", Role.RIDER)
        assertEquals(DeepLinkTarget.Screen(RiderTrip("trip-456")), riderTarget)
    }

    @Test
    fun parsesRateLink() {
        val target = DeepLinks.parse("hitchy://rate/ride-789", Role.RIDER)
        assertEquals(DeepLinkTarget.Screen(RateTrip("ride-789")), target)
    }

    @Test
    fun parsesFixedLink() {
        val target = DeepLinks.parse("hitchy://fixed/fixed-101", Role.RIDER)
        assertEquals(DeepLinkTarget.Screen(FixedRideDetails("fixed-101")), target)
    }

    @Test
    fun parsesBackupLink() {
        val target = DeepLinks.parse("hitchy://backup/cancelled-202", Role.RIDER)
        assertEquals(DeepLinkTarget.Screen(BackupOptions(cancelledRideId = "cancelled-202")), target)
    }

    @Test
    fun parsesReceiptLink() {
        val target = DeepLinks.parse("hitchy://receipt/ride-303", Role.RIDER)
        assertEquals(DeepLinkTarget.Screen(Receipt("ride-303")), target)
    }

    @Test
    fun parsesHostStatusLink() {
        val target = DeepLinks.parse("hitchy://host/status", Role.HOST)
        assertEquals(DeepLinkTarget.Screen(HostStatus), target)
    }

    @Test
    fun parsesChatLink() {
        val target = DeepLinks.parse("hitchy://chat/ride-999/rider-888", Role.HOST)
        assertEquals(DeepLinkTarget.Screen(Chat(rideId = "ride-999", riderUid = "rider-888")), target)
    }

    // --- Valid https:// links (Browser) ---

    @Test
    fun parsesHttpsBrowserLink() {
        val url = "https://admin.hitchy.app/alerts/42"
        val target = DeepLinks.parse(url, Role.RIDER)
        assertEquals(DeepLinkTarget.Browser(url), target)
    }

    @Test
    fun parsesHttpsWithQueryParams() {
        val url = "https://hitchy.app/help?topic=safety"
        val target = DeepLinks.parse(url, Role.RIDER)
        assertEquals(DeepLinkTarget.Browser(url), target)
    }

    // --- Invalid / Bad inputs returning null ---

    @Test
    fun returnsNullForBlankAndEmpty() {
        assertNull(DeepLinks.parse("", Role.RIDER))
        assertNull(DeepLinks.parse("   ", Role.RIDER))
    }

    @Test
    fun returnsNullForNonHttpsWebSchemes() {
        assertNull(DeepLinks.parse("http://admin.hitchy.app", Role.RIDER))
        assertNull(DeepLinks.parse("javascript:alert(1)", Role.RIDER))
        assertNull(DeepLinks.parse("ftp://hitchy.app/file", Role.RIDER))
        assertNull(DeepLinks.parse("custom://link", Role.RIDER))
    }

    @Test
    fun returnsNullForHttpsWithoutHost() {
        assertNull(DeepLinks.parse("https://", Role.RIDER))
        assertNull(DeepLinks.parse("https:///", Role.RIDER))
        assertNull(DeepLinks.parse("https:///alerts/123", Role.RIDER))
        assertNull(DeepLinks.parse("https:// invalid host", Role.RIDER))
    }

    @Test
    fun returnsNullForIncompleteHitchyUri() {
        assertNull(DeepLinks.parse("hitchy://", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy:///", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://ride", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://ride/", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://trip", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://trip/", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://rate", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://fixed", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://backup", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://receipt", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://host", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://chat", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://chat/ride-1", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://chat/ride-1/", Role.RIDER))
    }

    @Test
    fun returnsNullForExtraSegments() {
        assertNull(DeepLinks.parse("hitchy://home/extra", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://ride/r1/extra", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://trip/t1/extra", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://host/status/extra", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://chat/r1/u1/extra", Role.RIDER))
    }

    @Test
    fun returnsNullForEmptySegmentsInside() {
        assertNull(DeepLinks.parse("hitchy://ride//r1", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://chat//u1", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://chat/r1//", Role.RIDER))
    }

    @Test
    fun returnsNullForQueryStringsAndFragments() {
        assertNull(DeepLinks.parse("hitchy://home?tab=profile", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://ride/r1?source=notif", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://home#top", Role.RIDER))
    }

    @Test
    fun returnsNullForUnknownPaths() {
        assertNull(DeepLinks.parse("hitchy://unknown", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://host/unknown", Role.RIDER))
        assertNull(DeepLinks.parse("hitchy://something/else", Role.RIDER))
    }
}
