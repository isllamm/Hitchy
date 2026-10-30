@file:Suppress("MagicNumber")

package com.hitchy.core.ui.icon

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IconCatalogTest {

    private val expectedIcons = listOf(
        "back",
        "more",
        "bell",
        "mail",
        "lock",
        "person",
        "eye",
        "check",
        "scooter",
        "motorbike",
        "add_person",
        "id_card_verified",
        "upload",
        "camera",
        "car",
        "clock",
        "calendar",
        "search",
        "close",
        "chevron_down",
        "location_dot",
        "pin",
        "swap",
        "star",
        "shield_check",
        "info",
        "warning",
        "error",
        "success",
        "repeat",
        "home",
        "rides",
        "profile",
    )

    private val mirroredIcons = setOf("back", "swap")

    private fun findDrawableDir(): File {
        val candidates = listOf(
            File("src/commonMain/composeResources/drawable"),
            File("shared/src/commonMain/composeResources/drawable"),
            File("android/shared/src/commonMain/composeResources/drawable"),
        )
        return candidates.firstOrNull { it.isDirectory }
            ?: error("Could not locate composeResources/drawable directory")
    }

    @Test
    fun testAllExpectedIconsRegistered() {
        assertEquals(33, HitchyIcons.all.size)
        assertEquals(expectedIcons.sorted(), HitchyIcons.all.keys.sorted())
    }

    @Test
    fun testAllVectorFilesExistAndConformToDesignSpec() {
        val drawableDir = findDrawableDir()
        for (name in expectedIcons) {
            val file = File(drawableDir, "ic_$name.xml")
            assertTrue(file.exists(), "Missing icon file: ${file.absolutePath}")

            val content = file.readText()
            assertTrue(content.contains("<vector"), "Icon ic_$name.xml must be an Android vector drawable")
            assertTrue(content.contains("android:width=\"24dp\""), "Icon ic_$name.xml must have width 24dp")
            assertTrue(content.contains("android:height=\"24dp\""), "Icon ic_$name.xml must have height 24dp")
            assertTrue(
                content.contains("android:viewportWidth=\"24\""),
                "Icon ic_$name.xml must have viewportWidth 24",
            )
            assertTrue(
                content.contains("android:viewportHeight=\"24\""),
                "Icon ic_$name.xml must have viewportHeight 24",
            )
            assertTrue(content.contains("android:strokeWidth=\"2\""), "Icon ic_$name.xml must have strokeWidth 2")

            if (name in mirroredIcons) {
                assertTrue(
                    content.contains("android:autoMirrored=\"true\""),
                    "Directional icon ic_$name.xml must have autoMirrored=\"true\"",
                )
            }
        }
    }

    @Test
    fun testFigmaDrawingsExist() {
        val drawableDir = findDrawableDir()
        for (name in listOf("fig_driver", "fig_rider", "fig_arrow_forward") + listOf("fig_chevron_right", "fig_pin_small", "fig_calendar_small", "fig_clock_small", "fig_passenger_small", "fig_search_small", "fig_minus", "fig_plus", "fig_bell", "fig_tab_home_on", "fig_tab_home_off", "fig_tab_rides_on", "fig_tab_rides_off", "fig_tab_shop_on", "fig_tab_shop_off", "fig_toast_error", "fig_close_small", "fig_close", "fig_check_small", "fig_chevron_left_month", "fig_info_small", "fig_pickup", "fig_destination", "fig_swap", "fig_clear", "fig_map", "fig_place", "fig_back", "fig_chevron_right_20")) {
            val file = File(drawableDir, "$name.xml")
            assertTrue(file.exists(), "Missing Figma drawing: ${file.absolutePath}")
            assertTrue(file.readText().contains("<vector"), "$name.xml must be an Android vector drawable")
        }
        assertTrue(
            File(drawableDir, "fig_arrow_forward.xml").readText().contains("android:autoMirrored=\"true\""),
            "The onboarding arrow must mirror in right-to-left layouts",
        )
    }
}
