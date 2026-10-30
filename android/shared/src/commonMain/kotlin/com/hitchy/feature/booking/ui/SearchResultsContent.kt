@file:Suppress("MagicNumber", "TooManyFunctions")

package com.hitchy.feature.booking.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.ui.components.Avatar
import com.hitchy.core.ui.components.AvatarSize
import com.hitchy.core.ui.components.BackButton
import com.hitchy.core.ui.components.BackButtonStyle
import com.hitchy.core.ui.components.Badge
import com.hitchy.core.ui.components.BadgeVariant
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.HitchyButtonVariant
import com.hitchy.core.ui.components.SeatIndicator
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.map.HitchyMap
import com.hitchy.core.ui.map.MapPin
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.util.isoToCairoTime
import com.hitchy.feature.booking.domain.RideCard
import com.hitchy.feature.booking.domain.RideHost
import com.hitchy.feature.booking.domain.SearchInput
import com.hitchy.feature.places.domain.PlacesRepository
import com.hitchy.feature.places.domain.Point
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.bk_arrival
import hitchy.shared.generated.resources.bk_best_match
import hitchy.shared.generated.resources.bk_edit_route
import hitchy.shared.generated.resources.bk_egp_amount
import hitchy.shared.generated.resources.bk_empty_body
import hitchy.shared.generated.resources.bk_finding_body
import hitchy.shared.generated.resources.bk_finding_title
import hitchy.shared.generated.resources.bk_pickup_walk
import hitchy.shared.generated.resources.bk_prices_per_seat
import hitchy.shared.generated.resources.bk_rating_line
import hitchy.shared.generated.resources.bk_rides_match_many
import hitchy.shared.generated.resources.bk_rides_match_one
import hitchy.shared.generated.resources.bk_search_again
import hitchy.shared.generated.resources.bk_total_per_seat
import hitchy.shared.generated.resources.bk_verified_owner
import hitchy.shared.generated.resources.bk_view_ride
import hitchy.shared.generated.resources.search_results_change_time
import hitchy.shared.generated.resources.search_results_empty_title
import hitchy.shared.generated.resources.search_results_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private val FindingMapHeight = 330.dp
private val FindingSheetTop = 280.dp
private val FindingSheetRadius = 28.dp
private val ResultCardRadius = 24.dp

/** Names and the pickup point used by the three search screens, resolved from the zones and points lists. */
internal data class SearchSummary(
    val fromZoneName: String,
    val fromPointName: String,
    val toZoneName: String,
    val fromPoint: Point?,
    val input: SearchInput,
)

@Composable
internal fun rememberSearchSummary(
    input: SearchInput,
    language: AppLanguage,
): SearchSummary {
    val places: PlacesRepository = koinInject()
    val zoneFlow = remember(places) { places.observeZones() }
    val pointFlow = remember(places) { places.observeAllPoints() }
    val zones by zoneFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val points by pointFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val fromPoint = points.firstOrNull { it.id == input.fromPointId }
    val fromZoneName = zones.firstOrNull { it.id == input.fromZone }?.name?.resolve(language).orEmpty()
    return SearchSummary(
        fromZoneName = fromZoneName,
        fromPointName = fromPoint?.name?.resolve(language) ?: fromZoneName,
        toZoneName = zones.firstOrNull { it.id == input.toZone }?.name?.resolve(language).orEmpty(),
        fromPoint = fromPoint,
        input = input,
    )
}

@Composable
private fun SearchSummaryCard(summary: SearchSummary) {
    BookingTripCard(
        fromName = summary.fromZoneName,
        toName = summary.toZoneName,
        metaText = bookingMetaText(summary.input.date, summary.input.time, summary.input.seats),
    )
}

/** The "Finding rides" state: a map with the pickup point, and a sheet with the trip and two grey placeholders. */
@Composable
internal fun SearchFindingContent(
    summary: SearchSummary,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    Box(modifier = modifier.fillMaxSize().background(colors.white)) {
        HitchyMap(
            pins = listOfNotNull(summary.fromPoint?.let { MapPin(id = "from", point = it.location) }),
            modifier = Modifier.fillMaxWidth().height(FindingMapHeight),
        )
        BackButton(
            onClick = onBack,
            style = BackButtonStyle.OutlinedCircle,
            modifier = Modifier.statusBarsPadding().padding(start = 8.dp, top = 8.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = FindingSheetTop)
                .clip(RoundedCornerShape(topStart = FindingSheetRadius, topEnd = FindingSheetRadius))
                .background(colors.white)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FindingSheetHeader()
            SearchSummaryCard(summary)
            SkeletonRow()
            SkeletonRow()
        }
    }
}

@Composable
private fun FindingSheetHeader() {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Box(
        modifier = Modifier
            .size(width = 40.dp, height = 4.dp)
            .clip(CircleShape)
            .background(colors.border),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(colors.purple))
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(colors.violet))
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(colors.lavender))
    }
    Text(text = stringResource(Res.string.bk_finding_title), style = typography.h1, color = colors.navy)
    Text(text = stringResource(Res.string.bk_finding_body), style = typography.body, color = colors.textSecondary)
}

@Composable
private fun SkeletonRow() {
    val colors = HitchyTheme.colors
    BookingCardSurface {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.disabledSurface))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBar(width = 160.dp)
                SkeletonBar(width = 220.dp)
            }
        }
    }
}

@Composable
private fun SkeletonBar(width: Dp) {
    Box(
        modifier = Modifier
            .size(width = width, height = 10.dp)
            .clip(CircleShape)
            .background(HitchyTheme.colors.disabledSurface),
    )
}

/** The result list: trip summary, the count line and one card per ride. */
@Composable
internal fun SearchResultsBody(
    summary: SearchSummary,
    rides: List<RideCard>,
    language: AppLanguage,
    onBack: () -> Unit,
    onOpenRideDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().background(HitchyTheme.colors.white)) {
        BookingHeaderBar(title = stringResource(Res.string.search_results_title), onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { SearchSummaryCard(summary) }
            item { SearchCountRow(count = rides.size) }
            items(rides, key = { it.rideId }) { card ->
                SearchResultCard(card = card, language = language, onClick = { onOpenRideDetails(card.rideId) })
            }
        }
    }
}

@Composable
private fun SearchCountRow(count: Int) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = if (count == 1) {
                stringResource(Res.string.bk_rides_match_one)
            } else {
                stringResource(Res.string.bk_rides_match_many, count)
            },
            style = typography.bodyStrong,
            color = colors.navy,
        )
        Text(
            text = stringResource(Res.string.bk_prices_per_seat),
            style = typography.caption,
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun SearchResultCard(
    card: RideCard,
    language: AppLanguage,
    onClick: () -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    BookingCardSurface(background = colors.lilac, borderColor = colors.lavender, radius = ResultCardRadius) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (card.bestMatch) BestMatchPill()
            ResultTimesRow(card)
            ResultRouteLines(card, language)
            HorizontalDivider(color = colors.border, thickness = 1.dp)
            ResultHostRow(card.host)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.bk_total_per_seat),
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                Text(
                    text = stringResource(Res.string.bk_egp_amount, card.pricePerSeat),
                    style = typography.title,
                    color = colors.navy,
                )
            }
            HitchyButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(Res.string.bk_view_ride))
            }
        }
    }
}

@Composable
private fun BestMatchPill() {
    val colors = HitchyTheme.colors
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(colors.lavender)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            painter = painterResource(HitchyIcons.Star),
            contentDescription = null,
            tint = colors.purple,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = stringResource(Res.string.bk_best_match),
            style = HitchyTheme.typography.captionStrong,
            color = colors.purple,
        )
    }
}

@Composable
private fun ResultTimesRow(card: RideCard) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = isoToCairoTime(card.pickup.at), style = typography.h3, color = colors.navy)
        Icon(
            painter = painterResource(HitchyIcons.ArrowForward),
            contentDescription = null,
            tint = colors.purple,
            modifier = Modifier.size(12.dp),
        )
        Text(text = isoToCairoTime(card.arriveAt), style = typography.h3, color = colors.navy)
        Text(
            text = stringResource(Res.string.bk_arrival),
            style = typography.caption,
            color = colors.textSecondary,
            maxLines = 1,
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            SeatIndicator(availableSeats = card.seatsLeft)
        }
    }
}

@Composable
private fun ResultRouteLines(
    card: RideCard,
    language: AppLanguage,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ResultLine {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .border(2.dp, colors.purple, CircleShape),
            )
            Text(
                text = stringResource(
                    Res.string.bk_pickup_walk,
                    "\u2068${card.pickup.name.resolve(language)}\u2069",
                    card.pickup.walkM,
                ),
                style = typography.body,
                color = colors.navy,
                maxLines = 1,
            )
        }
        ResultLine {
            Icon(
                painter = painterResource(HitchyIcons.Pin),
                contentDescription = null,
                tint = colors.pin,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = card.toPointName.resolve(language),
                style = typography.body,
                color = colors.navy,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ResultLine(content: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        content()
    }
}

@Composable
private fun ResultHostRow(host: RideHost) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Avatar(initials = host.firstName.take(2), size = AvatarSize.LG)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = host.firstName, style = typography.bodyStrong, color = colors.navy, maxLines = 1)
            host.rating?.let { rating ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        painter = painterResource(HitchyIcons.Star),
                        contentDescription = null,
                        tint = colors.warning,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = stringResource(Res.string.bk_rating_line, rating.toString().take(3), host.trips),
                        style = typography.caption,
                        color = colors.textSecondary,
                    )
                }
            }
        }
        if (host.verified) {
            Badge(variant = BadgeVariant.Verified, label = stringResource(Res.string.bk_verified_owner))
        }
    }
}

/** The "No rides found" state: icon, text, and three ways to try again. */
@Composable
internal fun SearchEmptyContent(
    summary: SearchSummary,
    onBack: () -> Unit,
    onSearchAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    Column(modifier = modifier.fillMaxSize().background(colors.white)) {
        BookingHeaderBar(title = stringResource(Res.string.search_results_title), onBack = onBack)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            SearchSummaryCard(summary)
            SearchEmptyMessage(summary)
        }
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HitchyButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(Res.string.search_results_change_time))
            }
            HitchyButton(
                onClick = onBack,
                variant = HitchyButtonVariant.Outline,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(Res.string.bk_edit_route))
            }
            HitchyButton(
                onClick = onSearchAgain,
                variant = HitchyButtonVariant.Text,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(Res.string.bk_search_again))
            }
        }
    }
}

@Composable
private fun SearchEmptyMessage(summary: SearchSummary) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(96.dp).clip(CircleShape).background(colors.lavender),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(HitchyIcons.Car),
                contentDescription = null,
                tint = colors.purple,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.search_results_empty_title),
            style = typography.h1,
            color = colors.navy,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(
                Res.string.bk_empty_body,
                summary.fromPointName,
                summary.toZoneName,
                summary.input.time,
                bookingDateLabel(summary.input.date),
            ),
            style = typography.body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}
