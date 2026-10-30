package com.hitchy.core.ui.icon

import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.fig_arrow_forward
import hitchy.shared.generated.resources.fig_back
import hitchy.shared.generated.resources.fig_bell
import hitchy.shared.generated.resources.fig_calendar_small
import hitchy.shared.generated.resources.fig_cancelled
import hitchy.shared.generated.resources.fig_car_28
import hitchy.shared.generated.resources.fig_check_small
import hitchy.shared.generated.resources.fig_chevron_left_month
import hitchy.shared.generated.resources.fig_chevron_right
import hitchy.shared.generated.resources.fig_chevron_right_20
import hitchy.shared.generated.resources.fig_clear
import hitchy.shared.generated.resources.fig_clock_small
import hitchy.shared.generated.resources.fig_close
import hitchy.shared.generated.resources.fig_close_small
import hitchy.shared.generated.resources.fig_destination
import hitchy.shared.generated.resources.fig_driver
import hitchy.shared.generated.resources.fig_info_16
import hitchy.shared.generated.resources.fig_info_small
import hitchy.shared.generated.resources.fig_map
import hitchy.shared.generated.resources.fig_minus
import hitchy.shared.generated.resources.fig_no_rides
import hitchy.shared.generated.resources.fig_passenger_small
import hitchy.shared.generated.resources.fig_pickup
import hitchy.shared.generated.resources.fig_pin_small
import hitchy.shared.generated.resources.fig_place
import hitchy.shared.generated.resources.fig_plus
import hitchy.shared.generated.resources.fig_rider
import hitchy.shared.generated.resources.fig_search_small
import hitchy.shared.generated.resources.fig_swap
import hitchy.shared.generated.resources.fig_tab_home_off
import hitchy.shared.generated.resources.fig_tab_home_on
import hitchy.shared.generated.resources.fig_tab_rides_off
import hitchy.shared.generated.resources.fig_tab_rides_on
import hitchy.shared.generated.resources.fig_tab_shop_off
import hitchy.shared.generated.resources.fig_tab_shop_on
import hitchy.shared.generated.resources.fig_toast_error
import hitchy.shared.generated.resources.ic_add_person
import hitchy.shared.generated.resources.ic_back
import hitchy.shared.generated.resources.ic_bell
import hitchy.shared.generated.resources.ic_calendar
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
import hitchy.shared.generated.resources.fig_chevron_18
import hitchy.shared.generated.resources.fig_destination_22
import hitchy.shared.generated.resources.fig_home_22
import hitchy.shared.generated.resources.fig_menu_bell
import hitchy.shared.generated.resources.fig_menu_rides
import hitchy.shared.generated.resources.fig_menu_settings
import hitchy.shared.generated.resources.fig_person_22
import hitchy.shared.generated.resources.fig_back_22
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
    val Calendar: DrawableResource get() = Res.drawable.ic_calendar
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

    /** Figma's 10x10.5 chevron at the end of a form row; mirrors in right-to-left layouts. */
    val ChevronRightSmall: DrawableResource get() = Res.drawable.fig_chevron_right

    /** Figma's 10.5x14 destination pin in a form tile. */
    val PinSmall: DrawableResource get() = Res.drawable.fig_pin_small

    /** Figma's 10.5x12 calendar in the Date tile. */
    val CalendarSmall: DrawableResource get() = Res.drawable.fig_calendar_small

    /** Figma's 12x12 clock in the Departure time tile. */
    val ClockSmall: DrawableResource get() = Res.drawable.fig_clock_small

    /** Figma's 10.5x12 person in the Passengers tile. */
    val PassengerSmall: DrawableResource get() = Res.drawable.fig_passenger_small

    /** Figma's 14x14 magnifier on the Search rides button. */
    val SearchSmall: DrawableResource get() = Res.drawable.fig_search_small

    /** Figma's 18x18 stepper minus. */
    val Minus: DrawableResource get() = Res.drawable.fig_minus

    /** Figma's 18x18 stepper plus. */
    val Plus: DrawableResource get() = Res.drawable.fig_plus

    /** Figma's 20x20 notification bell on Home. */
    val BellLinear: DrawableResource get() = Res.drawable.fig_bell

    /** Figma's 24dp tab icons: bold when the tab is open, linear otherwise. */
    val TabHomeOn: DrawableResource get() = Res.drawable.fig_tab_home_on
    val TabHomeOff: DrawableResource get() = Res.drawable.fig_tab_home_off
    val TabRidesOn: DrawableResource get() = Res.drawable.fig_tab_rides_on
    val TabRidesOff: DrawableResource get() = Res.drawable.fig_tab_rides_off
    val TabShopOn: DrawableResource get() = Res.drawable.fig_tab_shop_on
    val TabShopOff: DrawableResource get() = Res.drawable.fig_tab_shop_off

    /** Figma's 20x20 error mark and 18x18 close on a toast. */
    val ToastError: DrawableResource get() = Res.drawable.fig_toast_error
    val CloseSmall: DrawableResource get() = Res.drawable.fig_close_small

    /** Figma's 22x22 sheet close, 16x16 chip check, 22x22 month chevron (points back) and 18x18 info. */
    val SheetClose: DrawableResource get() = Res.drawable.fig_close
    val CheckSmall: DrawableResource get() = Res.drawable.fig_check_small
    val ChevronBack: DrawableResource get() = Res.drawable.fig_chevron_left_month
    val InfoSmall: DrawableResource get() = Res.drawable.fig_info_small

    /** Figma's 20dp route icons; ClearField keeps its two colours and is drawn untinted; BackArrow mirrors. */
    val Pickup: DrawableResource get() = Res.drawable.fig_pickup
    val Destination: DrawableResource get() = Res.drawable.fig_destination
    val Swap20: DrawableResource get() = Res.drawable.fig_swap
    val ClearField: DrawableResource get() = Res.drawable.fig_clear
    val MapFold: DrawableResource get() = Res.drawable.fig_map
    val Place: DrawableResource get() = Res.drawable.fig_place
    val BackArrow: DrawableResource get() = Res.drawable.fig_back
    val ChevronRight20: DrawableResource get() = Res.drawable.fig_chevron_right_20

    /** Figma's 44x44 car outline on the No rides found state. */
    val NoRides: DrawableResource get() = Res.drawable.fig_no_rides

    /** Figma's 28x28 car outline on the ride's vehicle row. */
    val Car28: DrawableResource get() = Res.drawable.fig_car_28

    /** Figma's 16dp info mark (cancel sheet) and 30dp check (booking cancelled). */
    val Info16: DrawableResource get() = Res.drawable.fig_info_16
    val CancelledCheck: DrawableResource get() = Res.drawable.fig_cancelled

    /** Figma's Profile icons: 22dp info rows, 20dp menu rows, 18dp chevron (mirrors). */
    val Person22: DrawableResource get() = Res.drawable.fig_person_22
    val Home22: DrawableResource get() = Res.drawable.fig_home_22
    val Destination22: DrawableResource get() = Res.drawable.fig_destination_22
    val MenuRides: DrawableResource get() = Res.drawable.fig_menu_rides
    val MenuBell: DrawableResource get() = Res.drawable.fig_menu_bell
    val MenuSettings: DrawableResource get() = Res.drawable.fig_menu_settings
    val Chevron18: DrawableResource get() = Res.drawable.fig_chevron_18

    /** Figma's 22dp plain back arrow (Notifications header); mirrors. */
    val Back22: DrawableResource get() = Res.drawable.fig_back_22

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
        "calendar" to Calendar,
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
