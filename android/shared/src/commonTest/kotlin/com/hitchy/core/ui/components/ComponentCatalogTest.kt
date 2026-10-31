package com.hitchy.core.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComponentCatalogTest {

    @Test
    fun `button variants cover all 5 styles`() {
        val expected = setOf("Primary", "Tonal", "Outline", "Text", "Destructive")
        val actual = HitchyButtonVariant.entries.map { it.name }.toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun `avatar sizes cover 4 tokens from Figma`() {
        val expected = setOf("XS", "SM", "MD", "LG")
        val actual = AvatarSize.entries.map { it.name }.toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun `badge variants cover verified and pending`() {
        val expected = setOf("Verified", "Pending")
        val actual = BadgeVariant.entries.map { it.name }.toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun `banner variants cover 4 semantic categories`() {
        val expected = setOf("Success", "Error", "Warning", "Info")
        val actual = BannerVariant.entries.map { it.name }.toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun `trip stepper steps match product rule section 3_6 row 3`() {
        val expected = listOf("OwnerOnTheWay", "Arrived", "InRide", "Completed")
        val actual = TripStep.entries.map { it.name }
        assertEquals(expected, actual)
    }

    @Test
    fun `bottom nav covers four tabs decided in v1_1`() {
        val expected = listOf("Home", "Rides", "Shop", "Profile")
        val actual = BottomNavItem.entries.map { it.name }
        assertEquals(expected, actual)
    }

    @Test
    fun `status variants match plan section 3_6 row 4`() {
        val expected = setOf(
            "Requested",
            "Confirmed",
            "InProgress",
            "Completed",
            "Cancelled",
            "Declined",
            "Expired",
            "NoShow",
        )
        val actual = RideStatusVariant.entries.map { it.name }.toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun `role card covers rider and host`() {
        val expected = setOf("Rider", "Host")
        val actual = AppRoleType.entries.map { it.name }.toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun `trip action types cover 5 quick actions`() {
        val expected = setOf("Message", "Call", "ShareTrip", "Report", "Emergency")
        val actual = TripActionType.entries.map { it.name }.toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun `community badge types cover women-only, company and 3 tiers`() {
        val expected = setOf("WomenOnly", "SameCompany", "BronzeTier", "SilverTier", "GoldTier")
        val actual = CommunityBadgeType.entries.map { it.name }.toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun `chat bubble types cover sent, received, and assistant`() {
        val expected = setOf("Sent", "Received", "Assistant")
        val actual = ChatBubbleType.entries.map { it.name }.toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun `skeleton tones cover the white screen and the brand header`() {
        val expected = setOf("OnLight", "OnBrand")
        val actual = SkeletonTone.entries.map { it.name }.toSet()
        assertEquals(expected, actual)
    }
}
