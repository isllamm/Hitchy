package com.hitchy.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.hitchy.core.data.AppError
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.error_active_trip_soon
import hitchy.shared.generated.resources.error_ai_daily_limit
import hitchy.shared.generated.resources.error_ai_unavailable
import hitchy.shared.generated.resources.error_already_booked
import hitchy.shared.generated.resources.error_booking_not_found
import hitchy.shared.generated.resources.error_cannot_report_self
import hitchy.shared.generated.resources.error_chat_closed
import hitchy.shared.generated.resources.error_check_in_closed
import hitchy.shared.generated.resources.error_contact_not_allowed
import hitchy.shared.generated.resources.error_daily_ride_limit
import hitchy.shared.generated.resources.error_depart_too_far
import hitchy.shared.generated.resources.error_depart_too_soon
import hitchy.shared.generated.resources.error_fee_exists
import hitchy.shared.generated.resources.error_fee_not_disputable
import hitchy.shared.generated.resources.error_fee_not_found
import hitchy.shared.generated.resources.error_fixed_ride_exists
import hitchy.shared.generated.resources.error_fixed_ride_full
import hitchy.shared.generated.resources.error_gender_locked
import hitchy.shared.generated.resources.error_grace_not_over
import hitchy.shared.generated.resources.error_host_not_approved
import hitchy.shared.generated.resources.error_host_not_late
import hitchy.shared.generated.resources.error_internal
import hitchy.shared.generated.resources.error_invalid_input
import hitchy.shared.generated.resources.error_invite_invalid
import hitchy.shared.generated.resources.error_kyc_already_submitted
import hitchy.shared.generated.resources.error_kyc_missing_files
import hitchy.shared.generated.resources.error_kyc_not_submitted
import hitchy.shared.generated.resources.error_network
import hitchy.shared.generated.resources.error_no_credits
import hitchy.shared.generated.resources.error_not_enough_points
import hitchy.shared.generated.resources.error_not_found
import hitchy.shared.generated.resources.error_not_on_ride
import hitchy.shared.generated.resources.error_order_not_pending
import hitchy.shared.generated.resources.error_otp_expired
import hitchy.shared.generated.resources.error_otp_invalid
import hitchy.shared.generated.resources.error_own_ride
import hitchy.shared.generated.resources.error_package_not_found
import hitchy.shared.generated.resources.error_payment_already_confirmed
import hitchy.shared.generated.resources.error_permission_denied
import hitchy.shared.generated.resources.error_phone_required
import hitchy.shared.generated.resources.error_price_out_of_range
import hitchy.shared.generated.resources.error_profile_required
import hitchy.shared.generated.resources.error_rating_exists
import hitchy.shared.generated.resources.error_rating_not_allowed
import hitchy.shared.generated.resources.error_rating_window_closed
import hitchy.shared.generated.resources.error_request_expired
import hitchy.shared.generated.resources.error_request_not_pending
import hitchy.shared.generated.resources.error_ride_full
import hitchy.shared.generated.resources.error_ride_has_started
import hitchy.shared.generated.resources.error_ride_not_open
import hitchy.shared.generated.resources.error_ride_not_started
import hitchy.shared.generated.resources.error_ride_time_conflict
import hitchy.shared.generated.resources.error_rider_was_on_time
import hitchy.shared.generated.resources.error_route_not_supported
import hitchy.shared.generated.resources.error_scooter_disabled
import hitchy.shared.generated.resources.error_seats_invalid
import hitchy.shared.generated.resources.error_skip_too_late
import hitchy.shared.generated.resources.error_sms_quota
import hitchy.shared.generated.resources.error_start_window_closed
import hitchy.shared.generated.resources.error_suspended
import hitchy.shared.generated.resources.error_time_conflict
import hitchy.shared.generated.resources.error_too_late_to_book
import hitchy.shared.generated.resources.error_too_many_attempts
import hitchy.shared.generated.resources.error_too_many_pending
import hitchy.shared.generated.resources.error_unauthenticated
import hitchy.shared.generated.resources.error_unpaid_fee
import hitchy.shared.generated.resources.error_vehicle_limit
import hitchy.shared.generated.resources.error_vehicle_not_approved
import hitchy.shared.generated.resources.error_women_only
import hitchy.shared.generated.resources.error_women_only_host
import hitchy.shared.generated.resources.error_feature_disabled
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

fun AppError.message(): StringResource {
    return when (this) {
        is AppError.Network -> Res.string.error_network
        is AppError.NotSignedIn -> Res.string.error_unauthenticated
        is AppError.Reason -> {
            when (code.uppercase()) {
                "UNAUTHENTICATED" -> Res.string.error_unauthenticated
                "PERMISSION_DENIED" -> Res.string.error_permission_denied
                "INVALID_INPUT" -> Res.string.error_invalid_input
                "PROFILE_REQUIRED" -> Res.string.error_profile_required
                "SUSPENDED" -> Res.string.error_suspended
                "NOT_FOUND" -> Res.string.error_not_found
                "FEATURE_DISABLED" -> Res.string.error_feature_disabled
                "INTERNAL" -> Res.string.error_internal
                "PHONE_REQUIRED" -> Res.string.error_phone_required
                "GENDER_LOCKED" -> Res.string.error_gender_locked
                "HOST_NOT_APPROVED" -> Res.string.error_host_not_approved
                "VEHICLE_LIMIT" -> Res.string.error_vehicle_limit
                "SCOOTER_DISABLED" -> Res.string.error_scooter_disabled
                "KYC_ALREADY_SUBMITTED" -> Res.string.error_kyc_already_submitted
                "KYC_MISSING_FILES" -> Res.string.error_kyc_missing_files
                "KYC_NOT_SUBMITTED" -> Res.string.error_kyc_not_submitted
                "VEHICLE_NOT_APPROVED" -> Res.string.error_vehicle_not_approved
                "ROUTE_NOT_SUPPORTED" -> Res.string.error_route_not_supported
                "SEATS_INVALID" -> Res.string.error_seats_invalid
                "DEPART_TOO_SOON" -> Res.string.error_depart_too_soon
                "DEPART_TOO_FAR" -> Res.string.error_depart_too_far
                "PRICE_OUT_OF_RANGE" -> Res.string.error_price_out_of_range
                "WOMEN_ONLY_HOST" -> Res.string.error_women_only_host
                "DAILY_RIDE_LIMIT" -> Res.string.error_daily_ride_limit
                "RIDE_TIME_CONFLICT" -> Res.string.error_ride_time_conflict
                "NO_CREDITS" -> Res.string.error_no_credits
                "RIDE_HAS_STARTED" -> Res.string.error_ride_has_started
                "RIDE_NOT_OPEN" -> Res.string.error_ride_not_open
                "RIDE_FULL" -> Res.string.error_ride_full
                "OWN_RIDE" -> Res.string.error_own_ride
                "ALREADY_BOOKED" -> Res.string.error_already_booked
                "UNPAID_FEE" -> Res.string.error_unpaid_fee
                "WOMEN_ONLY" -> Res.string.error_women_only
                "TIME_CONFLICT" -> Res.string.error_time_conflict
                "TOO_LATE_TO_BOOK" -> Res.string.error_too_late_to_book
                "BOOKING_NOT_FOUND" -> Res.string.error_booking_not_found
                "HOST_NOT_LATE" -> Res.string.error_host_not_late
                "START_WINDOW_CLOSED" -> Res.string.error_start_window_closed
                "RIDE_NOT_STARTED" -> Res.string.error_ride_not_started
                "CHECK_IN_CLOSED" -> Res.string.error_check_in_closed
                "NOT_ON_RIDE" -> Res.string.error_not_on_ride
                "GRACE_NOT_OVER" -> Res.string.error_grace_not_over
                "RIDER_WAS_ON_TIME" -> Res.string.error_rider_was_on_time
                "FEE_EXISTS" -> Res.string.error_fee_exists
                "FEE_NOT_FOUND" -> Res.string.error_fee_not_found
                "FEE_NOT_DISPUTABLE" -> Res.string.error_fee_not_disputable
                "RATING_NOT_ALLOWED" -> Res.string.error_rating_not_allowed
                "RATING_EXISTS" -> Res.string.error_rating_exists
                "RATING_WINDOW_CLOSED" -> Res.string.error_rating_window_closed
                "PACKAGE_NOT_FOUND" -> Res.string.error_package_not_found
                "TOO_MANY_PENDING" -> Res.string.error_too_many_pending
                "ORDER_NOT_PENDING" -> Res.string.error_order_not_pending
                "NOT_ENOUGH_POINTS" -> Res.string.error_not_enough_points
                "AI_DAILY_LIMIT" -> Res.string.error_ai_daily_limit
                "AI_UNAVAILABLE" -> Res.string.error_ai_unavailable
                "INVITE_INVALID" -> Res.string.error_invite_invalid
                "FIXED_RIDE_FULL" -> Res.string.error_fixed_ride_full
                "FIXED_RIDE_EXISTS" -> Res.string.error_fixed_ride_exists
                "SKIP_TOO_LATE" -> Res.string.error_skip_too_late
                "CANNOT_REPORT_SELF" -> Res.string.error_cannot_report_self
                "ACTIVE_TRIP_SOON" -> Res.string.error_active_trip_soon
                "REQUEST_NOT_PENDING" -> Res.string.error_request_not_pending
                "REQUEST_EXPIRED" -> Res.string.error_request_expired
                "CHAT_CLOSED" -> Res.string.error_chat_closed
                "CONTACT_NOT_ALLOWED" -> Res.string.error_contact_not_allowed
                "PAYMENT_ALREADY_CONFIRMED" -> Res.string.error_payment_already_confirmed
                "OTP_INVALID" -> Res.string.error_otp_invalid
                "OTP_EXPIRED" -> Res.string.error_otp_expired
                "TOO_MANY_ATTEMPTS" -> Res.string.error_too_many_attempts
                "SMS_QUOTA" -> Res.string.error_sms_quota
                "NETWORK" -> Res.string.error_network
                else -> Res.string.error_internal
            }
        }
        is AppError.Unknown -> Res.string.error_internal
    }
}

@Composable
fun ErrorText(
    error: AppError,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.error,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Text(
        text = stringResource(error.message()),
        modifier = modifier,
        color = color,
        style = style
    )
}
