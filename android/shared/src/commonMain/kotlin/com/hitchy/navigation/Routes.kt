package com.hitchy.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Welcome

@Serializable
data object PhoneEntry

@Serializable
data class OtpVerify(val phoneE164: String)

@Serializable
data object ProfileSetup

@Serializable
data object Home

@Serializable
data object Settings

@Serializable
data object HostApplication

@Serializable
data object HostStatus

@Serializable
data object AddVehicle

@Serializable
data object PostRide

@Serializable
data object MyRides

@Serializable
data class HostRideDetails(val rideId: String)

@Serializable
data object Search

@Serializable
data class SearchResults(
    val fromZone: String,
    val toZone: String,
    val fromPointId: String? = null,
    val date: String,
    val time: String,
    val seats: Int,
)

@Serializable
data class RiderRideDetails(val rideId: String)

@Serializable
data object MyTrips

@Serializable
data class HostTrip(val rideId: String)

@Serializable
data class RiderTrip(val rideId: String)

@Serializable
data class RateTrip(val rideId: String)

@Serializable
data object Fees

@Serializable
data object Packages

@Serializable
data class PaymentInstructions(val orderId: String)

@Serializable
data object CreditHistory

@Serializable
data object Rewards

@Serializable
data object Invite

@Serializable
data object Assistant

@Serializable
data object FixedRides

@Serializable
data object CreateFixedRide

@Serializable
data class FixedRideDetails(val fixedRideId: String)

@Serializable
data object JoinFixedRide

@Serializable
data class BackupOptions(val cancelledRideId: String)

@Serializable
data object EmergencyContact

@Serializable
data object DeleteAccount

@Serializable
data object Debug

@Serializable
data object Onboarding

@Serializable
data class RoutePicker(val field: String)

@Serializable
data class MapPointPicker(val field: String)

@Serializable
data object Notifications

@Serializable
data class Chat(val rideId: String, val riderUid: String)

@Serializable
data class Receipt(val rideId: String)

@Serializable
data object Shop
