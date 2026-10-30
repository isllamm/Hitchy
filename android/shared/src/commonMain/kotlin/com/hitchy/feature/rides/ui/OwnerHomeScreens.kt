package com.hitchy.feature.rides.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.RideStatus
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.components.EmptyState
import com.hitchy.core.ui.components.GreetingHeader
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.RideStatusVariant
import com.hitchy.core.ui.components.StatusChip
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.util.isoToCairoDateKey
import com.hitchy.core.util.isoToCairoTime
import com.hitchy.feature.rides.domain.Ride
import com.hitchy.feature.rides.presentation.OwnerHomeViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.owner_home_no_rides_body
import hitchy.shared.generated.resources.owner_home_no_rides_title
import hitchy.shared.generated.resources.owner_home_post_cta
import hitchy.shared.generated.resources.owner_home_post_cta_subtitle
import hitchy.shared.generated.resources.owner_home_seats_booked
import hitchy.shared.generated.resources.owner_home_upcoming_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OwnerHomeContent(
    onOpenNotifications: () -> Unit,
    onOpenPostRide: () -> Unit,
    onOpenHostRideDetails: (String) -> Unit,
    onOpenHostTrip: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OwnerHomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val languageSwitcher: LanguageSwitcher = koinInject()
    val language = languageSwitcher.getCurrentLanguage()
    val colors = HitchyTheme.colors
    val spacing = HitchyTheme.spacing
    val typography = HitchyTheme.typography

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = spacing.space16),
        verticalArrangement = Arrangement.spacedBy(spacing.space16),
    ) {
        item {
            Spacer(modifier = Modifier.height(spacing.space8))
            GreetingHeader(
                firstName = state.firstName.ifBlank { "Owner" },
                hasUnreadNotifications = state.hasUnreadNotifications,
                onNotificationsClick = onOpenNotifications,
            )
        }

        item {
            OwnerPostCtaCard(onOpenPostRide = onOpenPostRide)
        }

        item {
            Text(
                text = stringResource(Res.string.owner_home_upcoming_title),
                style = typography.title,
                color = colors.textPrimary,
            )
        }

        if (state.upcomingRides.isEmpty()) {
            item {
                EmptyState(
                    title = stringResource(Res.string.owner_home_no_rides_title),
                    message = stringResource(Res.string.owner_home_no_rides_body),
                    icon = HitchyIcons.Car,
                    modifier = Modifier.padding(vertical = spacing.space24),
                )
            }
        } else {
            items(state.upcomingRides, key = { it.id }) { ride ->
                OwnerRideCard(
                    ride = ride,
                    language = language,
                    onClick = {
                        if (ride.status == RideStatus.STARTED) {
                            onOpenHostTrip(ride.id)
                        } else {
                            onOpenHostRideDetails(ride.id)
                        }
                    },
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(spacing.space32))
        }
    }
}

@Composable
private fun OwnerPostCtaCard(
    onOpenPostRide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val spacing = HitchyTheme.spacing
    val typography = HitchyTheme.typography
    val shape = RoundedCornerShape(16.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, colors.border, shape),
        color = colors.white,
        shape = shape,
    ) {
        Column(
            modifier = Modifier.padding(spacing.space16),
            verticalArrangement = Arrangement.spacedBy(spacing.space12),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.space12),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.lavender),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(HitchyIcons.Car),
                        contentDescription = null,
                        tint = colors.purple,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.owner_home_post_cta),
                        style = typography.title,
                        color = colors.textPrimary,
                    )
                    Text(
                        text = stringResource(Res.string.owner_home_post_cta_subtitle),
                        style = typography.caption,
                        color = colors.textSecondary,
                    )
                }
            }

            HitchyButton(
                onClick = onOpenPostRide,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(Res.string.owner_home_post_cta))
            }
        }
    }
}

@Composable
private fun OwnerRideCard(
    ride: Ride,
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val spacing = HitchyTheme.spacing
    val typography = HitchyTheme.typography
    val shape = RoundedCornerShape(12.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, colors.border, shape)
            .clickable(onClick = onClick),
        color = colors.white,
        shape = shape,
    ) {
        Column(
            modifier = Modifier.padding(spacing.space16),
            verticalArrangement = Arrangement.spacedBy(spacing.space8),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${isoToCairoDateKey(ride.departAt)} · ${isoToCairoTime(ride.departAt)}",
                    style = typography.caption,
                    color = colors.textSecondary,
                )
                val statusVariant = when (ride.status) {
                    RideStatus.OPEN, RideStatus.FULL -> RideStatusVariant.Confirmed
                    RideStatus.STARTED -> RideStatusVariant.InProgress
                    RideStatus.FINISHED -> RideStatusVariant.Completed
                    RideStatus.CANCELLED -> RideStatusVariant.Cancelled
                    RideStatus.EXPIRED -> RideStatusVariant.Expired
                }
                StatusChip(
                    status = statusVariant,
                    customLabel = ride.status.name.lowercase().replaceFirstChar { it.uppercase() },
                )
            }

            Text(
                text = "${ride.fromPointName.resolve(language)} → ${ride.toPointName.resolve(language)}",
                style = typography.title,
                color = colors.textPrimary,
            )

            val bookedSeats = (ride.seatsTotal - ride.seatsLeft).coerceAtLeast(0)
            Text(
                text = stringResource(Res.string.owner_home_seats_booked, bookedSeats, ride.seatsTotal),
                style = typography.body,
                color = colors.purple,
            )
        }
    }
}
