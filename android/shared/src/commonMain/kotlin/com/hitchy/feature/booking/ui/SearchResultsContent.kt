@file:Suppress("MagicNumber", "TooManyFunctions")

package com.hitchy.feature.booking.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.ui.cairoTime12
import com.hitchy.core.ui.components.Avatar
import com.hitchy.core.ui.components.AvatarImage
import com.hitchy.core.ui.components.AvatarSize
import com.hitchy.core.ui.components.BackButton
import com.hitchy.core.ui.components.BackButtonStyle
import com.hitchy.core.ui.components.Badge
import com.hitchy.core.ui.components.BadgeVariant
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.HitchyButtonVariant
import com.hitchy.core.ui.components.SeatIndicator
import com.hitchy.core.ui.figmaBottom
import com.hitchy.core.ui.figmaTop
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.map.HitchyMap
import com.hitchy.core.ui.map.MapPin
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.tightLine
import com.hitchy.feature.booking.domain.RideCard
import com.hitchy.feature.booking.domain.RideHost
import com.hitchy.feature.booking.domain.SearchInput
import com.hitchy.feature.places.domain.PlacesRepository
import com.hitchy.feature.places.domain.Point
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.bk_arrival
import hitchy.shared.generated.resources.bk_best_match
import hitchy.shared.generated.resources.bk_cancel_search
import hitchy.shared.generated.resources.bk_card_dropoff
import hitchy.shared.generated.resources.bk_card_pickup
import hitchy.shared.generated.resources.bk_card_walk
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
import hitchy.shared.generated.resources.fig_result_best
import hitchy.shared.generated.resources.fig_result_dest
import hitchy.shared.generated.resources.fig_result_pickup
import hitchy.shared.generated.resources.fig_result_verified
import hitchy.shared.generated.resources.img_empty_rides
import hitchy.shared.generated.resources.search_results_change_time
import hitchy.shared.generated.resources.search_results_empty_title
import hitchy.shared.generated.resources.search_results_title
import hitchy.shared.generated.resources.seat_full
import hitchy.shared.generated.resources.seat_left_1
import hitchy.shared.generated.resources.seat_left_2
import hitchy.shared.generated.resources.seat_left_3
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

// Figma frame 449:4034 "Search · Finding rides".
private val FindingMapHeight = 422.9.dp
private val FindingSheetTop = 344.dp
private val FindingSheetRadius = 24.dp
private val FindingBackTop = 56.dp
private val SkeletonShape = RoundedCornerShape(16.dp)
private val ResultCardRadius = 16.dp

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
            modifier = Modifier.padding(start = 16.dp, top = figmaTop(FindingBackTop)).size(44.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = FindingSheetTop)
                .clip(RoundedCornerShape(topStart = FindingSheetRadius, topEnd = FindingSheetRadius))
                .background(colors.white)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = figmaBottom(48.dp)),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FindingSheetHeader()
            SearchSummaryCard(summary)
            SkeletonRow()
            SkeletonRow()
            HitchyButton(onClick = onBack, variant = HitchyButtonVariant.Text, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(Res.string.bk_cancel_search))
            }
        }
    }
}

@Composable
private fun FindingSheetHeader() {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth()
            .size(width = 40.dp, height = 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(colors.border),
    )
    // Three 8dp purple dots at 100%, 55% and 25%.
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(1f, 0.55f, 0.25f).forEach { alpha ->
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.purple.copy(alpha = alpha)))
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = stringResource(Res.string.bk_finding_title), style = typography.heroTitle(), color = colors.navy)
        Text(text = stringResource(Res.string.bk_finding_body), style = typography.body, color = colors.textSecondary)
    }
}

@Composable
private fun SkeletonRow() {
    val colors = HitchyTheme.colors
    // Figma: 74dp, pad 14, r=16, a 44dp circle and two 10dp bars (160 and 230 wide), all #F3F4F6.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .clip(SkeletonShape)
            .background(colors.white)
            .border(1.dp, colors.border, SkeletonShape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.disabledSurface))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonBar(width = 160.dp)
            SkeletonBar(width = 230.dp)
        }
    }
}

@Composable
private fun SkeletonBar(width: Dp) {
    Box(
        modifier = Modifier
            .size(width = width, height = 10.dp)
            .clip(RoundedCornerShape(5.dp))
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

/**
 * The demo's Ride Result Card (component 466:6007): lilac, a #E5E7EB stroke, r=16, pad 16, 12dp gaps. Best match,
 * the times with the seats left, the pickup and drop-off lines, a rule, the owner row with the verified badge,
 * the total per seat, and View ride. The v1.0 fee rows are not shown: in v1.2 the seat price is the whole price.
 */
@Composable
private fun SearchResultCard(
    card: RideCard,
    language: AppLanguage,
    onClick: () -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val shape = RoundedCornerShape(ResultCardRadius)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.lilac)
            .border(1.dp, colors.border, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (card.bestMatch) BestMatchPill()
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ResultTimesRow(card, modifier = Modifier.weight(1f))
            ResultSeats(card.seatsLeft)
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ResultPlace(
                Res.drawable.fig_result_pickup,
                card.pickup.name.resolve(language).let { name ->
                    if (card.pickup.walkM > 0) {
                        stringResource(Res.string.bk_card_walk, "\u2068$name\u2069", card.pickup.walkM)
                    } else {
                        name
                    }
                },
            )
            ResultPlace(Res.drawable.fig_result_dest, card.toPointName.resolve(language))
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        ResultHostRow(card.host)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.bk_total_per_seat),
                style = typography.bodyBold,
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

/** Figma's Best match chip: lavender, pad 2/8, a 12dp mark and Bold 12/18 purple. */
@Composable
private fun BestMatchPill() {
    val colors = HitchyTheme.colors
    Row(
        modifier = Modifier.clip(CircleShape).background(colors.lavender).padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Image(
            painter = painterResource(Res.drawable.fig_result_best),
            contentDescription = null,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = stringResource(Res.string.bk_best_match),
            style = HitchyTheme.typography.captionStrong,
            color = colors.purple,
        )
    }
}

/** Figma: "7:30 AM → 8:20 AM arrival", 18/26 Bold times, a purple 14/22 arrow, a 12/18 label. */
@Composable
private fun ResultTimesRow(card: RideCard, modifier: Modifier = Modifier) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = cairoTime12(card.pickup.at), style = typography.title, color = colors.navy, maxLines = 1)
        Text(text = "→", style = typography.body, color = colors.purple)
        Text(text = cairoTime12(card.arriveAt), style = typography.title, color = colors.navy, maxLines = 1)
        Text(
            text = stringResource(Res.string.bk_arrival),
            style = typography.caption,
            color = colors.textSecondary,
            maxLines = 1,
        )
    }
}

/** Figma's Available seats (466:5954): three 14dp dots 4dp apart (purple for a free seat), then Bold 12/18 gray. */
@Composable
private fun ResultSeats(seatsLeft: Int) {
    val colors = HitchyTheme.colors
    val free = seatsLeft.coerceIn(0, SEAT_DOTS)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(SEAT_DOTS) { i ->
                Box(Modifier.size(14.dp).clip(CircleShape).background(if (i < free) colors.purple else colors.border))
            }
        }
        Text(
            text = when (free) {
                0 -> stringResource(Res.string.seat_full)
                1 -> stringResource(Res.string.seat_left_1)
                2 -> stringResource(Res.string.seat_left_2)
                else -> stringResource(Res.string.seat_left_3)
            },
            style = HitchyTheme.typography.captionStrong,
            color = colors.textSecondary,
        )
    }
}

private const val SEAT_DOTS = 3

@Composable
private fun ResultPlace(icon: DrawableResource, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Image(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(16.dp))
        Text(text = text, style = HitchyTheme.typography.body, color = HitchyTheme.colors.navy, maxLines = 1)
    }
}

/** Figma's `Host row`: a 44dp lavender avatar, the name in Bold 14/22, "★ 4.9 · 128 rides" and the green badge. */
@Composable
private fun ResultHostRow(host: RideHost) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.lavender),
            contentAlignment = Alignment.Center,
        ) {
            AvatarImage(
                photoPath = host.photoPath,
                initials = host.firstName.take(2).uppercase(),
                textStyle = typography.bodyBold,
                textColor = colors.purple,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = host.firstName,
                style = typography.bodyBold,
                color = colors.navy,
                maxLines = 1,
                modifier = Modifier.tightLine(typography.bodyBold.fontSize),
            )
            host.rating?.let { rating ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = "★", style = typography.captionStrong, color = colors.warning)
                    Text(
                        text = stringResource(Res.string.bk_rating_line, rating.toString().take(3), host.trips),
                        style = typography.caption,
                        modifier = Modifier.tightLine(typography.caption.fontSize),
                        color = colors.textSecondary,
                    )
                }
            }
        }
        if (host.verified) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.successBg)
                    .padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.fig_result_verified),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = stringResource(Res.string.bk_verified_owner),
                    style = typography.captionStrong,
                    color = colors.successText,
                )
            }
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
    // Figma frame 449:4158: header, trip summary, the message 16dp below, and three buttons 46dp above the bottom.
    Column(modifier = modifier.fillMaxSize().background(colors.white)) {
        BookingHeaderBar(title = stringResource(Res.string.search_results_title), onBack = onBack)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SearchSummaryCard(summary)
            SearchEmptyMessage(summary)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = figmaBottom(46.dp)),
            verticalArrangement = Arrangement.spacedBy(10.dp),
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
    // The same picture, title and line as the empty Rides tab and the Shop tab.
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(Res.drawable.img_empty_rides),
            contentDescription = null,
            modifier = Modifier.size(width = 300.dp, height = 200.dp),
        )
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
                bookingTime12(summary.input.time),
                bookingDateLabel(summary.input.date),
            ),
            style = typography.bodyLarge,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(290.dp).padding(top = 8.78.dp),
        )
    }
}

/** Figma's Cairo 800 24/30 with -0.1 letter spacing: the Finding and No rides titles. */
internal fun com.hitchy.core.ui.theme.HitchyTypography.heroTitle() = amount.copy(letterSpacing = (-0.1).sp)
