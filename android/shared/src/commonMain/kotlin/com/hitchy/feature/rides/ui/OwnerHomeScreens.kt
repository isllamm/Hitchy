@file:Suppress("MagicNumber", "TooManyFunctions")

package com.hitchy.feature.rides.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.RideStatus
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.components.EmptyState
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.map.HitchyMap
import com.hitchy.core.ui.map.MapPin
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.util.dateKey
import com.hitchy.feature.profile.domain.WalletRepository
import com.hitchy.feature.rides.domain.Ride
import com.hitchy.feature.rides.presentation.OwnerHomeState
import com.hitchy.feature.rides.presentation.OwnerHomeViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.greeting_morning
import hitchy.shared.generated.resources.img_brand_logo
import hitchy.shared.generated.resources.owner_home_greeting
import hitchy.shared.generated.resources.owner_home_no_rides_body
import hitchy.shared.generated.resources.owner_home_no_rides_title
import hitchy.shared.generated.resources.owner_home_post_cta
import hitchy.shared.generated.resources.owner_home_upcoming_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

private val MapHeight = 330.dp
private val SheetTopOffset = 280.dp
private val SheetRadius = 28.dp

/**
 * Owner Home built like the rider Home: the map on top with the logo and the bell, and a rounded sheet with
 * the greeting, the wallet balance, the Offer a ride button and the next rides.
 */
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
    val nextRide = state.upcomingRides.firstOrNull {
        it.status == RideStatus.OPEN || it.status == RideStatus.FULL || it.status == RideStatus.STARTED
    }

    Box(modifier = modifier.fillMaxSize().background(HitchyTheme.colors.background)) {
        HitchyMap(
            pins = listOfNotNull(nextRide?.let { MapPin(id = "from", point = it.fromPoint) }),
            modifier = Modifier.fillMaxWidth().height(MapHeight),
        )
        OwnerHomeTopRow(hasUnread = state.hasUnreadNotifications, onOpenNotifications = onOpenNotifications)
        OwnerHomeSheet(
            state = state,
            onOpenPostRide = onOpenPostRide,
            onOpenHostRideDetails = onOpenHostRideDetails,
            onOpenHostTrip = onOpenHostTrip,
            modifier = Modifier.fillMaxSize().padding(top = SheetTopOffset),
        )
    }
}

@Composable
private fun OwnerHomeTopRow(
    hasUnread: Boolean,
    onOpenNotifications: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.img_brand_logo),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.height(48.dp),
        )
        OwnerBellButton(hasUnread = hasUnread, onClick = onOpenNotifications)
    }
}

@Composable
private fun OwnerHomeSheet(
    state: OwnerHomeState,
    onOpenPostRide: () -> Unit,
    onOpenHostRideDetails: (String) -> Unit,
    onOpenHostTrip: (String) -> Unit,
    modifier: Modifier = Modifier,
    languageSwitcher: LanguageSwitcher = koinInject(),
    walletRepository: WalletRepository = koinInject(),
    clock: Clock = koinInject(),
) {
    val colors = HitchyTheme.colors
    val language = languageSwitcher.getCurrentLanguage()
    val balanceFlow = remember(walletRepository) { walletRepository.observeBalance() }
    val balance by balanceFlow.collectAsStateWithLifecycle(initialValue = 0)
    val now = clock.now()
    val todayKey = dateKey(now)
    val tomorrowKey = dateKey(now + 1.days)

    LazyColumn(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = SheetRadius, topEnd = SheetRadius))
            .background(colors.white),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { SheetHandle() }
        item { OwnerGreeting(firstName = state.firstName) }
        item { OwnerWalletCard(balanceEgp = balance) }
        item {
            OwnerGradientButton(
                text = stringResource(Res.string.owner_home_post_cta),
                onClick = onOpenPostRide,
                icon = HitchyIcons.Car,
            )
        }
        item {
            Text(
                text = stringResource(Res.string.owner_home_upcoming_title),
                style = HitchyTheme.typography.h2,
                color = colors.navy,
            )
        }
        ownerRideItems(
            rides = state.upcomingRides,
            language = language,
            todayKey = todayKey,
            tomorrowKey = tomorrowKey,
            onOpenHostRideDetails = onOpenHostRideDetails,
            onOpenHostTrip = onOpenHostTrip,
        )
    }
}

@Suppress("LongParameterList")
private fun LazyListScope.ownerRideItems(
    rides: List<Ride>,
    language: AppLanguage,
    todayKey: String,
    tomorrowKey: String,
    onOpenHostRideDetails: (String) -> Unit,
    onOpenHostTrip: (String) -> Unit,
) {
    if (rides.isEmpty()) {
        item {
            EmptyState(
                title = stringResource(Res.string.owner_home_no_rides_title),
                message = stringResource(Res.string.owner_home_no_rides_body),
                icon = HitchyIcons.Car,
                modifier = Modifier.padding(vertical = 24.dp),
            )
        }
        return
    }
    items(rides, key = { it.id }) { ride ->
        OwnerRideCard(
            ride = ride,
            language = language,
            todayKey = todayKey,
            tomorrowKey = tomorrowKey,
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

@Composable
private fun SheetHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 4.dp)
                .clip(CircleShape)
                .background(HitchyTheme.colors.border),
        )
    }
}

@Composable
private fun OwnerGreeting(firstName: String) {
    val text = if (firstName.isBlank()) {
        stringResource(Res.string.greeting_morning)
    } else {
        stringResource(Res.string.owner_home_greeting, firstName)
    }
    Text(text = text, style = HitchyTheme.typography.h2, color = HitchyTheme.colors.navy)
}
