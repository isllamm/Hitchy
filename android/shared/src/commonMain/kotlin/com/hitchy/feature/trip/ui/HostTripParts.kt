@file:Suppress("MagicNumber", "TooManyFunctions")

package com.hitchy.feature.trip.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.ui.cairoTime12
import com.hitchy.core.ui.components.Avatar
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.tightLine
import com.hitchy.feature.booking.ui.RideSummaryRow
import com.hitchy.feature.rides.domain.Booking
import com.hitchy.feature.trip.presentation.HostTripPhase
import com.hitchy.feature.trip.presentation.HostTripState
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.host_trip_payment_done_bookings
import hitchy.shared.generated.resources.host_trip_payment_done_title
import hitchy.shared.generated.resources.host_trip_payment_done_total
import hitchy.shared.generated.resources.host_trip_wallet_balance
import hitchy.shared.generated.resources.owner_design_chip_dropped_off
import hitchy.shared.generated.resources.owner_design_chip_here_now
import hitchy.shared.generated.resources.owner_design_chip_next
import hitchy.shared.generated.resources.owner_design_chip_on_board
import hitchy.shared.generated.resources.owner_design_chip_on_board_late
import hitchy.shared.generated.resources.owner_design_chip_waiting
import hitchy.shared.generated.resources.owner_design_free_wait
import hitchy.shared.generated.resources.owner_design_late_fee
import hitchy.shared.generated.resources.owner_design_late_fee_applies
import hitchy.shared.generated.resources.owner_design_step_completed
import hitchy.shared.generated.resources.owner_design_step_leaving
import hitchy.shared.generated.resources.owner_design_step_on_the_way
import hitchy.shared.generated.resources.owner_design_step_pickups
import hitchy.shared.generated.resources.owner_design_timer_label
import hitchy.shared.generated.resources.owner_design_timer_value
import hitchy.shared.generated.resources.rides_design_vehicle
import hitchy.shared.generated.resources.rides_t14_seats_booked
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val CardRadius = 16.dp
private const val SECONDS_PER_MINUTE = 60
private const val NAME_FALLBACK_LENGTH = 8
private const val TRACK_ALPHA = 0.2f
private const val LABEL_ALPHA = 0.7f

/** The rider's first name, or a short id piece while the profile is still loading. */
internal fun riderDisplayName(names: Map<String, String>, uid: String): String {
    val known = names[uid]
    if (known != null) return known
    return uid.take(NAME_FALLBACK_LENGTH).replaceFirstChar { it.uppercase() }
}

/** The rider the owner is dealing with now: the one picked in the list, else the first who is not on board. */
internal fun currentRider(bookings: List<Booking>, selectedUid: String?, pickupId: String? = null): Booking? {
    val picked = bookings.firstOrNull { it.riderUid == selectedUid && it.boardedAt == null }
    if (picked != null) return picked
    // At a pickup only its own riders are waited for; without one, anyone not on board yet.
    if (pickupId != null) return bookings.firstOrNull { it.boardedAt == null && it.pickupId == pickupId }
    return bookings.firstOrNull { it.boardedAt == null }
}

internal enum class PillTone { Success, Warning, Info }

@Composable
internal fun OwnerPill(
    text: String,
    tone: PillTone,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val (background, content) = when (tone) {
        PillTone.Success -> colors.successBg to colors.successText
        PillTone.Warning -> colors.warningBg to colors.warningText
        PillTone.Info -> colors.lavender to colors.purple
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(text = text, style = HitchyTheme.typography.captionStrong, color = content, maxLines = 1)
    }
}

/** Figma's header chip ("3 riders", "1 on board"): 28dp, padding 12, Regular 12/18. */
@Composable
internal fun CountChip(text: String, tone: PillTone) {
    val colors = HitchyTheme.colors
    val (background, content) = when (tone) {
        PillTone.Success -> colors.successBg to colors.success
        PillTone.Warning -> colors.warningBg to colors.warningText
        PillTone.Info -> colors.lavender to colors.purple
    }
    Box(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = HitchyTheme.typography.caption, color = content, maxLines = 1)
    }
}

/** Four steps: Leaving, Pickups, On the way, Completed. [currentIndex] 4 or more means every step is done. */
@Composable
internal fun OwnerTripProgress(
    currentIndex: Int,
    modifier: Modifier = Modifier,
) {
    val labels = listOf(
        stringResource(Res.string.owner_design_step_leaving),
        stringResource(Res.string.owner_design_step_pickups),
        stringResource(Res.string.owner_design_step_on_the_way),
        stringResource(Res.string.owner_design_step_completed),
    )
    Row(modifier = modifier.fillMaxWidth()) {
        labels.forEachIndexed { index, label ->
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    ProgressLine(
                        visible = index > 0,
                        done = index <= currentIndex,
                        modifier = Modifier.weight(1f),
                    )
                    ProgressDot(done = index < currentIndex, current = index == currentIndex)
                    ProgressLine(
                        visible = index < labels.lastIndex,
                        done = index + 1 <= currentIndex,
                        modifier = Modifier.weight(1f),
                    )
                }
                val reached = index <= currentIndex
                Text(
                    text = label,
                    style = if (reached) HitchyTheme.typography.captionStrong else HitchyTheme.typography.caption,
                    color = if (reached) HitchyTheme.colors.purple else HitchyTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = 8.dp),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ProgressLine(
    visible: Boolean,
    done: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val color = when {
        !visible -> Color.Transparent
        done -> colors.purple
        else -> colors.border
    }
    Box(modifier = modifier.height(2.dp).background(color))
}

@Composable
private fun ProgressDot(
    done: Boolean,
    current: Boolean,
) {
    val colors = HitchyTheme.colors
    Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
        when {
            done -> Box(
                modifier = Modifier.size(24.dp).clip(CircleShape).background(colors.purple),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(HitchyIcons.Check),
                    contentDescription = null,
                    tint = colors.white,
                    modifier = Modifier.size(14.dp),
                )
            }

            current -> Box(
                modifier = Modifier.size(24.dp).clip(CircleShape).background(colors.lavender),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(colors.purple))
            }

            else -> Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(colors.white)
                    .border(2.dp, colors.border, CircleShape),
            )
        }
    }
}

/** One card with a row per booked rider and a status pill on the right. */
@Composable
internal fun RiderStatusCard(
    state: HostTripState,
    language: AppLanguage,
    currentUid: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val shape = RoundedCornerShape(CardRadius)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.white)
            .border(1.dp, colors.border, shape)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        state.bookings.forEach { booking ->
            RiderStatusRow(
                booking = booking,
                name = riderDisplayName(state.riderNames, booking.riderUid),
                photoPath = state.riderPhotos[booking.riderUid],
                phase = state.phase,
                isCurrent = booking.riderUid == currentUid,
                language = language,
                onClick = { onSelect(booking.riderUid) },
            )
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        }
        state.ride?.let { ride ->
            // Figma's card ends with the ride's summary row, as on the Ride Card.
            Box(Modifier.padding(top = 4.dp, bottom = 12.dp)) {
                RideSummaryRow(
                    tileIcon = HitchyIcons.Person,
                    title = stringResource(
                        Res.string.rides_t14_seats_booked,
                        state.bookings.sumOf { it.seats },
                        ride.seatsTotal,
                    ),
                    subtitle = stringResource(
                        Res.string.rides_design_vehicle,
                        ride.vehicle.make,
                        ride.vehicle.model,
                        ride.vehicle.color,
                    ),
                    priceEgp = ride.pricePerSeat,
                    perSeat = true,
                )
            }
        }
    }
}

@Composable
private fun RiderStatusRow(
    booking: Booking,
    name: String,
    photoPath: String?,
    phase: HostTripPhase,
    isCurrent: Boolean,
    language: AppLanguage,
    onClick: () -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val canSelect = phase == HostTripPhase.AT_STOP && booking.boardedAt == null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = canSelect, onClick = onClick)
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Avatar(initials = name.take(2).uppercase(), photoPath = photoPath)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = name, style = typography.bodyStrong, color = colors.navy, maxLines = 1, modifier = Modifier.tightLine(typography.bodyStrong.fontSize))
            Text(
                text = "${booking.pickupPointName.resolve(language)} · ${cairoTime12(booking.pickupAt)}",
                style = typography.caption,
                modifier = Modifier.tightLine(typography.caption.fontSize),
                color = colors.textSecondary,
                maxLines = 1,
            )
        }
        RiderPill(booking = booking, phase = phase, isCurrent = isCurrent)
    }
}

@Composable
private fun RiderPill(
    booking: Booking,
    phase: HostTripPhase,
    isCurrent: Boolean,
) {
    val boarded = booking.boardedAt != null
    val atEnd = phase == HostTripPhase.ARRIVED || phase == HostTripPhase.PAYMENT_DONE
    when {
        atEnd -> if (boarded) {
            OwnerPill(stringResource(Res.string.owner_design_chip_dropped_off), PillTone.Success)
        }

        boarded && booking.lateFeeId != null ->
            OwnerPill(stringResource(Res.string.owner_design_chip_on_board_late), PillTone.Warning)

        boarded -> OwnerPill(stringResource(Res.string.owner_design_chip_on_board), PillTone.Success)

        phase == HostTripPhase.EN_ROUTE -> Unit

        phase == HostTripPhase.DRIVING_TO_STOP && isCurrent ->
            OwnerPill(stringResource(Res.string.owner_design_chip_next), PillTone.Info)

        phase == HostTripPhase.AT_STOP && isCurrent ->
            OwnerPill(stringResource(Res.string.owner_design_chip_here_now), PillTone.Info)

        else -> OwnerPill(stringResource(Res.string.owner_design_chip_waiting), PillTone.Warning)
    }
}

/** The dark timer card shown while the owner waits at a stop: elapsed time, free wait bar and the late fee note. */
@Composable
internal fun WaitTimerCard(
    state: HostTripState,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val free = HostTripState.FREE_WAIT_SECONDS
    val over = state.isFreeWaitOver
    val elapsed = if (over) free + state.lateSeconds else free - state.freeWaitSecondsLeft
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.navy)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.owner_design_timer_label),
                style = typography.body,
                color = colors.white.copy(alpha = LABEL_ALPHA),
            )
            if (over) {
                OwnerPill(stringResource(Res.string.owner_design_late_fee_applies), PillTone.Warning)
            }
        }
        Text(
            text = stringResource(
                Res.string.owner_design_timer_value,
                (elapsed / SECONDS_PER_MINUTE).toString().padStart(2, '0'),
                (elapsed % SECONDS_PER_MINUTE).toString().padStart(2, '0'),
            ),
            style = typography.h1,
            color = colors.white,
        )
        TimerBar(fraction = elapsed.toFloat() / free, over = over)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = stringResource(Res.string.owner_design_free_wait, free / SECONDS_PER_MINUTE),
                style = typography.caption,
                color = colors.white.copy(alpha = LABEL_ALPHA),
            )
            Text(
                text = stringResource(Res.string.owner_design_late_fee),
                style = typography.caption,
                color = colors.warning,
            )
        }
    }
}

@Composable
private fun TimerBar(fraction: Float, over: Boolean) {
    val colors = HitchyTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape)
            .background(colors.white.copy(alpha = TRACK_ALPHA)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(6.dp)
                .background(if (over) colors.warning else colors.purple),
        )
    }
}

/** What the owner sees after the payment is confirmed: the total collected and the wallet balance. */
@Composable
internal fun PaymentSummaryCard(
    state: HostTripState,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val shape = RoundedCornerShape(CardRadius)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.white)
            .border(1.dp, colors.border, shape)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(colors.lavender),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(HitchyIcons.Check),
                contentDescription = null,
                tint = colors.purple,
                modifier = Modifier.size(32.dp),
            )
        }
        Text(
            text = stringResource(Res.string.host_trip_payment_done_title),
            style = typography.h2,
            color = colors.navy,
        )
        Text(
            text = stringResource(Res.string.host_trip_payment_done_total, state.paymentTotalCollected),
            style = typography.title,
            color = colors.purple,
        )
        Text(
            text = stringResource(Res.string.host_trip_payment_done_bookings, state.paymentBookingsCount),
            style = typography.body,
            color = colors.textSecondary,
        )
        Text(
            text = stringResource(Res.string.host_trip_wallet_balance, state.walletBalance),
            style = typography.title,
            color = colors.navy,
        )
    }
}
