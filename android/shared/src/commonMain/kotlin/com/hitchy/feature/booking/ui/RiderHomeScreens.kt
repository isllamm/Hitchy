package com.hitchy.feature.booking.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.Role
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.components.BannerVariant
import com.hitchy.core.ui.components.BottomNav
import com.hitchy.core.ui.components.BottomNavItem
import com.hitchy.core.ui.components.EmptyState
import com.hitchy.core.ui.components.GreetingHeader
import com.hitchy.core.ui.components.HitchyBanner
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.PassengersStepper
import com.hitchy.core.ui.components.RouteInput
import com.hitchy.core.ui.components.SelectChip
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.booking.presentation.RiderHomeAction
import com.hitchy.feature.booking.presentation.RiderHomeEvent
import com.hitchy.feature.booking.presentation.RiderHomeState
import com.hitchy.feature.booking.presentation.RiderHomeViewModel
import com.hitchy.feature.booking.presentation.RiderSearchValidationError
import com.hitchy.feature.profile.domain.ProfileRepository
import com.hitchy.feature.profile.ui.ProfileTabContent
import com.hitchy.feature.rides.ui.OwnerHomeContent
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.app_name
import hitchy.shared.generated.resources.nav_home
import hitchy.shared.generated.resources.nav_shop
import hitchy.shared.generated.resources.picker_time_sheet_title
import hitchy.shared.generated.resources.rider_home_date_today
import hitchy.shared.generated.resources.rider_home_date_tomorrow
import hitchy.shared.generated.resources.rider_home_from_placeholder
import hitchy.shared.generated.resources.rider_home_missing_route_error
import hitchy.shared.generated.resources.rider_home_passengers_label
import hitchy.shared.generated.resources.rider_home_plan_title
import hitchy.shared.generated.resources.rider_home_same_zone_error
import hitchy.shared.generated.resources.rider_home_search_button
import hitchy.shared.generated.resources.rider_home_time_label
import hitchy.shared.generated.resources.rider_home_to_placeholder
import hitchy.shared.generated.resources.rides_tab_title
import hitchy.shared.generated.resources.role_host
import hitchy.shared.generated.resources.role_host_desc
import hitchy.shared.generated.resources.shop_coming_soon_body
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    activeRole: Role,
    onOpenNotifications: () -> Unit,
    onOpenRoutePicker: (String) -> Unit,
    onNavigateToSearchResults: (RiderHomeEvent.NavigateToSearchResults) -> Unit,
    onOpenRiderRideDetails: (String) -> Unit,
    onOpenRiderTrip: (String) -> Unit,
    onOpenHostRideDetails: (String) -> Unit,
    onOpenHostTrip: (String) -> Unit,
    onOpenPostRide: () -> Unit,
    onOpenHostApplication: () -> Unit,
    modifier: Modifier = Modifier,
    profileRepository: ProfileRepository = koinInject(),
) {
    val colors = HitchyTheme.colors
    var selectedTab by remember { mutableStateOf(BottomNavItem.Home) }
    val me by profileRepository.observeMe().collectAsStateWithLifecycle(initialValue = null)
    // Figma's Profile tab shows the user's initials, "FT".
    val initials = me?.let { "${it.firstName.take(1)}${it.lastName.take(1)}".uppercase() }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    BottomNavItem.Home -> {
                        if (activeRole == Role.HOST) {
                            OwnerHomeContent(
                                onOpenNotifications = onOpenNotifications,
                                onOpenPostRide = onOpenPostRide,
                                onOpenRidesTab = { selectedTab = BottomNavItem.Rides },
                                onOpenEarnings = { selectedTab = BottomNavItem.Profile },
                                onOpenHostRideDetails = onOpenHostRideDetails,
                                onOpenHostTrip = onOpenHostTrip,
                            )
                        } else {
                            RiderHomeContent(
                                onOpenNotifications = onOpenNotifications,
                                onOpenRoutePicker = onOpenRoutePicker,
                                onNavigateToSearchResults = onNavigateToSearchResults,
                            )
                        }
                    }

                    BottomNavItem.Rides -> {
                        RiderRidesTabContent(
                            onBack = null,
                            onOpenRiderRideDetails = onOpenRiderRideDetails,
                            onOpenRiderTrip = onOpenRiderTrip,
                            onOpenHostRideDetails = onOpenHostRideDetails,
                            onOpenHostTrip = onOpenHostTrip,
                        )
                    }

                    BottomNavItem.Shop -> {
                        EmptyState(
                            title = stringResource(Res.string.nav_shop),
                            message = stringResource(Res.string.shop_coming_soon_body),
                            icon = HitchyIcons.Star,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    BottomNavItem.Profile -> {
                        ProfileTabContent(
                            onOpenMyRides = { selectedTab = BottomNavItem.Rides },
                            onOpenNotifications = onOpenNotifications,
                            onOpenHostApplication = onOpenHostApplication,
                        )
                    }
                }
            }

            BottomNav(
                selectedItem = selectedTab,
                onItemSelected = { selectedTab = it },
                userInitials = initials,
                onAddRide = if (activeRole == Role.HOST) onOpenPostRide else null,
            )
        }
    }
}

@Suppress("MagicNumber")
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TimeSlotSelector(
    selectedMinutes: Int,
    onSelectMinutes: (Int) -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val spacing = HitchyTheme.spacing
    // Popular commute slots in 15-min / 30-min steps
    val slots = remember {
        listOf(
            7 * 60,
            7 * 60 + 30,
            8 * 60,
            8 * 60 + 15,
            8 * 60 + 30,
            9 * 60,
            9 * 60 + 30,
            17 * 60,
            17 * 60 + 30,
            18 * 60,
            18 * 60 + 30,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.background)
            .padding(spacing.space12),
        verticalArrangement = Arrangement.spacedBy(spacing.space8),
    ) {
        Text(
            text = stringResource(Res.string.picker_time_sheet_title),
            style = typography.captionStrong,
            color = colors.navy,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.space8),
            verticalArrangement = Arrangement.spacedBy(spacing.space8),
        ) {
            slots.forEach { minutes ->
                val h = (minutes / 60).toString().padStart(2, '0')
                val m = (minutes % 60).toString().padStart(2, '0')
                SelectChip(
                    label = "$h:$m",
                    selected = selectedMinutes == minutes,
                    onClick = { onSelectMinutes(minutes) },
                )
            }
        }
    }
}
