@file:Suppress("MagicNumber")

package com.hitchy.feature.debug.ui.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.components.ActionTilesRow
import com.hitchy.core.ui.components.AppRoleType
import com.hitchy.core.ui.components.BalanceCard
import com.hitchy.core.ui.components.BannerVariant
import com.hitchy.core.ui.components.ChatBubble
import com.hitchy.core.ui.components.ChatBubbleType
import com.hitchy.core.ui.components.DepartureRow
import com.hitchy.core.ui.components.DocumentUploadTile
import com.hitchy.core.ui.components.EmptyState
import com.hitchy.core.ui.components.ErrorState
import com.hitchy.core.ui.components.HitchyBanner
import com.hitchy.core.ui.components.LateFeeCard
import com.hitchy.core.ui.components.NotificationRow
import com.hitchy.core.ui.components.OwnerRequestCard
import com.hitchy.core.ui.components.PassengersStepper
import com.hitchy.core.ui.components.PriceSlider
import com.hitchy.core.ui.components.PriceSummary
import com.hitchy.core.ui.components.PriceTag
import com.hitchy.core.ui.components.PriceTagSize
import com.hitchy.core.ui.components.RideCard
import com.hitchy.core.ui.components.RideStatusVariant
import com.hitchy.core.ui.components.RoleCard
import com.hitchy.core.ui.components.RouteTimeline
import com.hitchy.core.ui.components.SeatIndicator
import com.hitchy.core.ui.components.Skeleton
import com.hitchy.core.ui.components.SkeletonDetails
import com.hitchy.core.ui.components.SkeletonListRow
import com.hitchy.core.ui.components.SkeletonRideCard
import com.hitchy.core.ui.components.TripStep
import com.hitchy.core.ui.components.TripStepper
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GalleryCardsAndStatesSection(modifier: Modifier = Modifier) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current

    var passengerCount by remember { mutableStateOf(2) }
    var sliderPrice by remember { mutableStateOf(45) }
    var selectedRole by remember { mutableStateOf(AppRoleType.Rider) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.space16),
    ) {
        // Trip Cards & Booking
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.white),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(spacing.space16),
                verticalArrangement = Arrangement.spacedBy(spacing.space12),
            ) {
                Text(
                    text = "Trip & Booking Cards",
                    style = typography.h3,
                    color = colors.textPrimary,
                )

                Text(
                    text = "RideCard (Full Preview)",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                RideCard(
                    departureText = "Daily · Sun – Thu",
                    pickupTime = "07:30 AM",
                    pickupLocation = "Maadi Grand Mall",
                    dropOffTime = "08:15 AM",
                    dropOffLocation = "Smart Village Building B3",
                    hostName = "Ahmed Mahmoud",
                    hostInitials = "AM",
                    carModelColor = "Nissan Sunny · White",
                    priceEgp = 45,
                    availableSeats = 2,
                    onClick = {},
                    status = RideStatusVariant.Confirmed,
                )

                Text(
                    text = "RouteTimeline",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                RouteTimeline(
                    pickupTime = "07:30 AM",
                    pickupLocation = "Maadi Grand Mall",
                    dropOffTime = "08:15 AM",
                    dropOffLocation = "Smart Village Building B3",
                )

                Text(
                    text = "Departure & Seat Indicators",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                DepartureRow(departureText = "Tomorrow · 08:00 AM")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.space16),
                    verticalArrangement = Arrangement.spacedBy(spacing.space8),
                ) {
                    SeatIndicator(availableSeats = 3, totalSeats = 3)
                    SeatIndicator(availableSeats = 1, totalSeats = 3)
                    SeatIndicator(availableSeats = 0, totalSeats = 3)
                    PriceTag(amountEgp = 45, size = PriceTagSize.Large)
                    PriceTag(amountEgp = 45, size = PriceTagSize.Small)
                }

                Text(
                    text = "Price Summary & Passengers Stepper",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = "Seats needed", style = typography.body)
                    PassengersStepper(
                        count = passengerCount,
                        onCountChange = { passengerCount = it },
                    )
                }
                PriceSummary(
                    pricePerSeatEgp = 45,
                    seatsCount = passengerCount,
                )

                Text(
                    text = "PriceSlider (Interactive)",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                PriceSlider(
                    priceEgp = sliderPrice,
                    onPriceChange = { sliderPrice = it },
                    minPriceEgp = 30,
                    maxPriceEgp = 70,
                    fairPriceEgp = 45,
                )

                Text(
                    text = "OwnerRequestCard",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                OwnerRequestCard(
                    riderName = "Nour El-Din",
                    riderInitials = "NE",
                    riderRating = 5.0,
                    seatsRequested = 1,
                    deadlineRemaining = "12m",
                    onAccept = {},
                    onDecline = {},
                )

                Text(
                    text = "LateFeeCard",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                LateFeeCard(
                    amountEgp = 50,
                    reason = "Rider no-show on Oct 28",
                    onPayClick = {},
                )
            }
        }

        // Feedback, Banners & States
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.white),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(spacing.space16),
                verticalArrangement = Arrangement.spacedBy(spacing.space12),
            ) {
                Text(
                    text = "Feedback, Banners & States",
                    style = typography.h3,
                    color = colors.textPrimary,
                )

                Text(
                    text = "HitchyBanner Variants",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                HitchyBanner(
                    text = "Your booking request was sent to the owner.",
                    variant = BannerVariant.Info,
                )
                HitchyBanner(
                    text = "Payment confirmed! Your seat is reserved.",
                    variant = BannerVariant.Success,
                )
                HitchyBanner(
                    text = "Please arrive at the meeting point 5 minutes early.",
                    variant = BannerVariant.Warning,
                )
                HitchyBanner(
                    text = "Network connection lost. Please try again.",
                    variant = BannerVariant.Error,
                )

                Text(
                    text = "TripStepper (All 4 Steps)",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                TripStepper(currentStep = TripStep.OwnerOnTheWay)
                TripStepper(currentStep = TripStep.Arrived)
                TripStepper(currentStep = TripStep.InRide)
                TripStepper(currentStep = TripStep.Completed)

                Text(
                    text = "DocumentUploadTile",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                DocumentUploadTile(
                    label = "National ID (Front)",
                    onClick = {},
                    isUploaded = false,
                )
                DocumentUploadTile(
                    label = "Driver License",
                    onClick = {},
                    isUploaded = true,
                    fileName = "license_front.jpg",
                )

                Text(
                    text = "EmptyState & ErrorState",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                EmptyState(
                    title = "No rides found",
                    message = "Try searching for a different departure time or route.",
                    actionLabel = "Adjust search",
                    onActionClick = {},
                )
                ErrorState(
                    message = "Could not load ride details. Please check your connection.",
                    onRetry = {},
                )

                Text(
                    text = "Skeleton (shimmer)",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                Skeleton(verticalArrangement = Arrangement.spacedBy(spacing.space12)) {
                    SkeletonRideCard()
                    SkeletonListRow()
                }
                SkeletonDetails()
            }
        }

        // Communication, Tiles & Roles
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.white),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(spacing.space16),
                verticalArrangement = Arrangement.spacedBy(spacing.space12),
            ) {
                Text(
                    text = "Communication & Tiles",
                    style = typography.h3,
                    color = colors.textPrimary,
                )

                Text(
                    text = "ActionTilesRow",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                ActionTilesRow(
                    onMessage = {},
                    onCall = {},
                    onShare = {},
                    onEmergency = {},
                )

                Text(
                    text = "ChatBubble (Sent, Received, Assistant)",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                ChatBubble(
                    message = "Hello! I am waiting near the main gate.",
                    timestampText = "07:28 AM",
                    type = ChatBubbleType.Sent,
                )
                ChatBubble(
                    message = "Great, I will be there in 2 minutes.",
                    timestampText = "07:29 AM",
                    type = ChatBubbleType.Received,
                )
                ChatBubble(
                    message = "Ride reminder: departure in 15 minutes.",
                    timestampText = "07:15 AM",
                    type = ChatBubbleType.Assistant,
                )

                Text(
                    text = "NotificationRow",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                NotificationRow(
                    title = "Booking Accepted",
                    body = "Ahmed accepted your booking request for tomorrow at 07:30 AM.",
                    timestampText = "5m ago",
                    isUnread = true,
                    onClick = {},
                )
                NotificationRow(
                    title = "Trip Completed",
                    body = "Don't forget to rate your experience with Ahmed.",
                    timestampText = "2h ago",
                    isUnread = false,
                    onClick = {},
                )

                Text(
                    text = "RoleCard (Rider vs Owner)",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.space12),
                ) {
                    RoleCard(
                        role = AppRoleType.Rider,
                        selected = selectedRole == AppRoleType.Rider,
                        onClick = { selectedRole = AppRoleType.Rider },
                        modifier = Modifier.weight(1f),
                    )
                    RoleCard(
                        role = AppRoleType.Host,
                        selected = selectedRole == AppRoleType.Host,
                        onClick = { selectedRole = AppRoleType.Host },
                        modifier = Modifier.weight(1f),
                    )
                }

                Text(
                    text = "BalanceCard",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                BalanceCard(
                    creditsEgp = 350,
                    points = 120,
                )
            }
        }
    }
}
