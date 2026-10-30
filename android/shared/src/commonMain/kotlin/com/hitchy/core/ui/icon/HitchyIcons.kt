package com.hitchy.core.ui.icon

import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.fig_arrow_forward
import hitchy.shared.generated.resources.fig_driver
import hitchy.shared.generated.resources.fig_rider
import hitchy.shared.generated.resources.ic_add_person
import hitchy.shared.generated.resources.ic_back
import hitchy.shared.generated.resources.ic_bell
import hitchy.shared.generated.resources.ic_camera
import hitchy.shared.generated.resources.ic_car
import hitchy.shared.generated.resources.ic_check
import hitchy.shared.generated.resources.ic_chevron_down
import hitchy.shared.generated.resources.ic_clock
import hitchy.shared.generated.resources.ic_close
import hitchy.shared.generated.resources.ic_error
import hitchy.shared.generated.resources.ic_eye
import hitchy.shared.generated.resources.ic_home
import hitchy.shared.generated.resources.ic_id_card_verified
import hitchy.shared.generated.resources.ic_info
import hitchy.shared.generated.resources.ic_location_dot
import hitchy.shared.generated.resources.ic_lock
import hitchy.shared.generated.resources.ic_mail
import hitchy.shared.generated.resources.ic_more
import hitchy.shared.generated.resources.ic_motorbike
import hitchy.shared.generated.resources.ic_person
import hitchy.shared.generated.resources.ic_pin
import hitchy.shared.generated.resources.ic_profile
import hitchy.shared.generated.resources.ic_repeat
import hitchy.shared.generated.resources.ic_rides
import hitchy.shared.generated.resources.ic_scooter
import hitchy.shared.generated.resources.ic_search
import hitchy.shared.generated.resources.ic_shield_check
import hitchy.shared.generated.resources.ic_star
import hitchy.shared.generated.resources.ic_success
import hitchy.shared.generated.resources.ic_swap
import hitchy.shared.generated.resources.ic_upload
import hitchy.shared.generated.resources.ic_warning
import org.jetbrains.compose.resources.DrawableResource

/**
 * Hitchy design system icons (BUILD_PLAN section 3.4).
 *
 * Outlined, 24 dp, 2 dp stroke.
 */
object HitchyIcons {
    val Back: DrawableResource get() = Res.drawable.ic_back
    val More: DrawableResource get() = Res.drawable.ic_more
    val Bell: DrawableResource get() = Res.drawable.ic_bell
    val Mail: DrawableResource get() = Res.drawable.ic_mail
    val Lock: DrawableResource get() = Res.drawable.ic_lock
    val Person: DrawableResource get() = Res.drawable.ic_person
    val Eye: DrawableResource get() = Res.drawable.ic_eye
    val Check: DrawableResource get() = Res.drawable.ic_check
    val Scooter: DrawableResource get() = Res.drawable.ic_scooter
    val Motorbike: DrawableResource get() = Res.drawable.ic_motorbike
    val AddPerson: DrawableResource get() = Res.drawable.ic_add_person
    val IdCardVerified: DrawableResource get() = Res.drawable.ic_id_card_verified
    val Upload: DrawableResource get() = Res.drawable.ic_upload
    val Camera: DrawableResource get() = Res.drawable.ic_camera
    val Car: DrawableResource get() = Res.drawable.ic_car
    val Clock: DrawableResource get() = Res.drawable.ic_clock
    val Search: DrawableResource get() = Res.drawable.ic_search
    val Close: DrawableResource get() = Res.drawable.ic_close
    val ChevronDown: DrawableResource get() = Res.drawable.ic_chevron_down
    val LocationDot: DrawableResource get() = Res.drawable.ic_location_dot
    val Pin: DrawableResource get() = Res.drawable.ic_pin
    val Swap: DrawableResource get() = Res.drawable.ic_swap
    val Star: DrawableResource get() = Res.drawable.ic_star
    val ShieldCheck: DrawableResource get() = Res.drawable.ic_shield_check
    val Info: DrawableResource get() = Res.drawable.ic_info
    val Warning: DrawableResource get() = Res.drawable.ic_warning
    val Error: DrawableResource get() = Res.drawable.ic_error
    val Success: DrawableResource get() = Res.drawable.ic_success
    val Repeat: DrawableResource get() = Res.drawable.ic_repeat
    val Home: DrawableResource get() = Res.drawable.ic_home
    val Rides: DrawableResource get() = Res.drawable.ic_rides
    val Profile: DrawableResource get() = Res.drawable.ic_profile

    // Figma drawings kept at their own size and weight, outside the 24dp set above.

    /** Figma's 30dp DriverIcon, the "I have a vehicle" role card. */
    val Driver: DrawableResource get() = Res.drawable.fig_driver

    /** Figma's 30dp RiderIcon, the "I need a ride" role card. */
    val Rider: DrawableResource get() = Res.drawable.fig_rider

    /** Figma's 12dp onboarding next arrow; mirrors in right-to-left layouts. */
    val ArrowForward: DrawableResource get() = Res.drawable.fig_arrow_forward

    val all: Map<String, DrawableResource> get() = mapOf(
        "back" to Back,
        "more" to More,
        "bell" to Bell,
        "mail" to Mail,
        "lock" to Lock,
        "person" to Person,
        "eye" to Eye,
        "check" to Check,
        "scooter" to Scooter,
        "motorbike" to Motorbike,
        "add_person" to AddPerson,
        "id_card_verified" to IdCardVerified,
        "upload" to Upload,
        "camera" to Camera,
        "car" to Car,
        "clock" to Clock,
        "search" to Search,
        "close" to Close,
        "chevron_down" to ChevronDown,
        "location_dot" to LocationDot,
        "pin" to Pin,
        "swap" to Swap,
        "star" to Star,
        "shield_check" to ShieldCheck,
        "info" to Info,
        "warning" to Warning,
        "error" to Error,
        "success" to Success,
        "repeat" to Repeat,
        "home" to Home,
        "rides" to Rides,
        "profile" to Profile,
    )
}
