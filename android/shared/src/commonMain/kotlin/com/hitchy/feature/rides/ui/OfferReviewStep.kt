package com.hitchy.feature.rides.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.TrafficLevel
import com.hitchy.core.ui.components.Skeleton
import com.hitchy.core.ui.components.SkeletonBlock
import com.hitchy.core.ui.components.SkeletonLine
import com.hitchy.core.ui.figmaShadow
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.tight
import com.hitchy.feature.rides.presentation.PostRideAction
import com.hitchy.feature.rides.presentation.PostRideState
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.format_egp
import hitchy.shared.generated.resources.offer_currency
import hitchy.shared.generated.resources.offer_drop_off
import hitchy.shared.generated.resources.offer_edit
import hitchy.shared.generated.resources.offer_fair_price
import hitchy.shared.generated.resources.offer_fuel
import hitchy.shared.generated.resources.offer_full_car
import hitchy.shared.generated.resources.offer_full_car_covers
import hitchy.shared.generated.resources.offer_max
import hitchy.shared.generated.resources.offer_min
import hitchy.shared.generated.resources.offer_per_seat
import hitchy.shared.generated.resources.offer_per_seat_peak
import hitchy.shared.generated.resources.offer_recommended
import hitchy.shared.generated.resources.offer_recommended_per_seat
import hitchy.shared.generated.resources.offer_rider_pays
import hitchy.shared.generated.resources.offer_running_cost
import hitchy.shared.generated.resources.offer_seats_open
import hitchy.shared.generated.resources.offer_start
import hitchy.shared.generated.resources.offer_stop
import hitchy.shared.generated.resources.offer_trip_cost_split
import hitchy.shared.generated.resources.offer_trip_cost_value
import hitchy.shared.generated.resources.offer_women_only_tag
import hitchy.shared.generated.resources.offer_your_commute
import hitchy.shared.generated.resources.post_ride_quoting
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Figma's breakdown card shadow: #1F1C4A at 2%, 0/2 blur 4. */
private const val BREAKDOWN_SHADOW_ALPHA = 0.02f

/**
 * D4 "Review and publish" (Figma DEMO 726:11499): the price hero with its slider, the cost breakdown
 * from the quote, and the commute summary. Figma's ride-vibe tags come from the dropped D3 step, so only
 * the seats tag (and women only, when set) shows.
 */
@Composable
internal fun OfferReviewStep(
    state: PostRideState,
    language: AppLanguage,
    onEdit: () -> Unit,
    onAction: (PostRideAction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PriceHero(state, onAction)
        if (state.quote != null) {
            Breakdown(state)
        } else if (state.isQuoting) {
            BreakdownSkeleton()
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(Res.string.offer_your_commute),
                    style = HitchyTheme.typography.bodyBold,
                    color = HitchyTheme.colors.textSecondary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(Res.string.offer_edit),
                    style = HitchyTheme.typography.bodyStrong,
                    color = HitchyTheme.colors.purple,
                    modifier = Modifier.clickableShape(RoundedCornerShape(8.dp), onEdit).padding(4.dp),
                )
            }
            OfferCommuteSummary(stops = offerTimeline(state, language), tags = offerTags(state))
        }
    }
}

/** The cost breakdown card as grey blocks, while the quote is on its way. */
@Composable
private fun BreakdownSkeleton() {
    val colors = HitchyTheme.colors
    Skeleton(
        modifier = Modifier
            .fillMaxWidth()
            .clip(OfferCardShape)
            .background(colors.white)
            .border(1.dp, colors.hairline, OfferCardShape)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SkeletonLine(width = 180.dp, height = 16.dp)
        repeat(SKELETON_BREAKDOWN_ROWS) { SkeletonLine(width = 260.dp) }
    }
}

private const val SKELETON_BREAKDOWN_ROWS = 3

/** The posted route as Figma's timeline rows: start, the stops, then the drop-off (no arrival time yet). */
@Composable
internal fun offerTimeline(state: PostRideState, language: AppLanguage): List<TimelineStop> = buildList {
    add(
        TimelineStop(
            time = clock12(state.departTimeMinutes),
            label = stringResource(Res.string.offer_start),
            place = state.fromPoint?.name?.resolve(language).orEmpty(),
        ),
    )
    state.stops.forEach { stop ->
        val point = stop.point ?: return@forEach
        add(
            TimelineStop(
                time = clock12(state.departTimeMinutes + stop.minutesFromDepart),
                label = stringResource(Res.string.offer_stop, stop.index + 1),
                place = point.name.resolve(language),
            ),
        )
    }
    add(
        TimelineStop(
            time = "",
            label = stringResource(Res.string.offer_drop_off),
            place = state.toPoint?.name?.resolve(language).orEmpty(),
        ),
    )
}

@Composable
internal fun offerTags(state: PostRideState): List<String> = buildList {
    add(stringResource(Res.string.offer_seats_open, state.seats))
    if (state.womenOnly) add(stringResource(Res.string.offer_women_only_tag))
}

/**
 * Figma's `Price hero`: lilac, r16, padding 12/20/20/16. "EACH RIDER PAYS YOU" with the green "Fair price"
 * badge, the price in purple (EGP 600 14/22 + amount 800 24/30 + a 12/18 unit), the slider and its
 * Min / Recommended / Max labels.
 */
@Composable
private fun PriceHero(state: PostRideState, onAction: (PostRideAction) -> Unit) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val quote = state.quote
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(OfferCardShape)
            .background(colors.lilac)
            .padding(start = 16.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(26.dp)) {
            Text(
                text = stringResource(Res.string.offer_rider_pays),
                style = typography.captionStrong,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f),
            )
            if (quote != null) FairPriceBadge()
        }
        if (quote == null && state.isQuoting) {
            // The real price is still coming from the server: the figure and the slider shimmer.
            Skeleton(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SkeletonBlock(modifier = Modifier.size(width = 140.dp, height = 30.dp))
                SkeletonBlock(modifier = Modifier.fillMaxWidth().height(24.dp), shape = CircleShape)
                SkeletonLine(width = 200.dp)
            }
            return@Column
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(Res.string.offer_currency),
                style = typography.bodyStrong,
                color = colors.purple,
                modifier = Modifier.alignByBaseline(),
            )
            Text(
                text = state.pricePerSeat.toString(),
                style = typography.amount,
                color = colors.purple,
                modifier = Modifier.alignByBaseline(),
            )
            val peak = quote?.trafficLevel == TrafficLevel.PEAK
            Text(
                text = stringResource(if (peak) Res.string.offer_per_seat_peak else Res.string.offer_per_seat),
                style = typography.caption,
                color = colors.textSecondary,
                modifier = Modifier.alignByBaseline(),
            )
        }
        if (quote == null) {
            Text(
                text = stringResource(Res.string.post_ride_quoting),
                style = typography.caption,
                color = colors.textSecondary,
            )
        } else {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                OfferPriceSlider(value = state.pricePerSeat, min = quote.min, max = quote.max) {
                    onAction(PostRideAction.UpdatePrice(it))
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    PriceLabel(quote.min, stringResource(Res.string.offer_min), TextAlign.Start, Alignment.Start)
                    PriceLabel(
                        quote.recommended,
                        stringResource(Res.string.offer_recommended),
                        TextAlign.Center,
                        Alignment.CenterHorizontally,
                    )
                    PriceLabel(quote.max, stringResource(Res.string.offer_max), TextAlign.End, Alignment.End)
                }
            }
        }
    }
}

@Composable
private fun RowScope.PriceLabel(
    amount: Int,
    label: String,
    align: TextAlign,
    horizontal: Alignment.Horizontal,
) {
    Column(modifier = Modifier.weight(1f), horizontalAlignment = horizontal) {
        Text(
            text = stringResource(Res.string.format_egp, amount.toString()),
            style = HitchyTheme.typography.bodyStrong,
            color = HitchyTheme.colors.navy,
            textAlign = align,
        )
        Text(
            text = label,
            style = HitchyTheme.typography.caption,
            color = HitchyTheme.colors.textSecondary,
            textAlign = align,
        )
    }
}

/** Figma's green `Status Badge`: #DCFCE7, a 6dp dot and Bold 12/18 #166534, padding 4/10. */
@Composable
private fun FairPriceBadge() {
    val colors = HitchyTheme.colors
    Row(
        modifier = Modifier.clip(CircleShape).background(colors.successBg).padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(colors.successText))
        Text(
            text = stringResource(Res.string.offer_fair_price),
            style = HitchyTheme.typography.captionStrong.tight(),
            color = colors.successText,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Figma's `Breakdown` card (r16, #E9EEF5 stroke, padding 16/12, gap 12): the recommended seat price,
 * fuel, running cost and the trip cost split by seats, from the server's quote, then the green full-car toast.
 */
@Composable
private fun Breakdown(state: PostRideState) {
    val quote = state.quote ?: return
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val fullCar = state.pricePerSeat * state.seats
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .figmaShadow(OfferCardShape, colors.navy.copy(alpha = BREAKDOWN_SHADOW_ALPHA), blur = 4.dp, y = 2.dp)
            .clip(OfferCardShape)
            .background(colors.white)
            .border(1.dp, colors.hairline, OfferCardShape)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.offer_recommended_per_seat),
                style = typography.bodyBold,
                color = colors.navy,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(Res.string.format_egp, quote.recommended.toString()),
                style = typography.title,
                color = colors.purple,
            )
        }
        BreakdownRow(
            stringResource(Res.string.offer_fuel),
            stringResource(Res.string.format_egp, quote.fuelCostEgp.toString()),
        )
        Divider()
        BreakdownRow(
            stringResource(Res.string.offer_running_cost),
            stringResource(Res.string.format_egp, (quote.tripCostEgp - quote.fuelCostEgp).coerceAtLeast(0).toString()),
        )
        Divider()
        BreakdownRow(
            stringResource(Res.string.offer_trip_cost_split, state.seats),
            stringResource(Res.string.offer_trip_cost_value, quote.tripCostEgp, state.seats),
        )
        Divider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.successBg)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                painter = painterResource(HitchyIcons.CheckSmall),
                contentDescription = null,
                tint = colors.success,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = if (fullCar >= quote.tripCostEgp) {
                    stringResource(Res.string.offer_full_car_covers, fullCar)
                } else {
                    stringResource(Res.string.offer_full_car, fullCar)
                },
                style = typography.bodyStrong,
                color = colors.successText,
            )
        }
    }
}

@Composable
private fun BreakdownRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = HitchyTheme.typography.body,
            color = HitchyTheme.colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(text = value, style = HitchyTheme.typography.bodyStrong, color = HitchyTheme.colors.navy)
    }
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(HitchyTheme.colors.border))
}
