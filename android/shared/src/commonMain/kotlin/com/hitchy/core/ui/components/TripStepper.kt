package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.stepper_arrived
import hitchy.shared.generated.resources.stepper_completed
import hitchy.shared.generated.resources.stepper_in_ride
import hitchy.shared.generated.resources.stepper_on_the_way
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class TripStep {
    OwnerOnTheWay,
    Arrived,
    InRide,
    Completed,
}

@Composable
fun TripStepper(
    currentStep: TripStep,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current
    val steps = TripStep.entries

    val labels = mapOf(
        TripStep.OwnerOnTheWay to stringResource(Res.string.stepper_on_the_way),
        TripStep.Arrived to stringResource(Res.string.stepper_arrived),
        TripStep.InRide to stringResource(Res.string.stepper_in_ride),
        TripStep.Completed to stringResource(Res.string.stepper_completed),
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            steps.forEachIndexed { index, step ->
                val isDone = step.ordinal < currentStep.ordinal
                val isCurrent = step == currentStep
                val isUpcoming = step.ordinal > currentStep.ordinal

                // Step circle
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        isDone -> {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(colors.purple),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(HitchyIcons.Check),
                                    contentDescription = null,
                                    tint = colors.white,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }

                        isCurrent -> {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(colors.lavender)
                                    .border(2.dp, colors.purple, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(colors.purple),
                                )
                            }
                        }

                        isUpcoming -> {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(colors.disabledSurface)
                                    .border(1.5.dp, colors.border, CircleShape),
                            )
                        }
                    }
                }

                // Connector line
                if (index < steps.size - 1) {
                    val lineDone = step.ordinal < currentStep.ordinal
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(if (lineDone) colors.purple else colors.border),
                    )
                }
            }
        }

        // Labels row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacing.space8),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            steps.forEach { step ->
                val isCurrent = step == currentStep
                val isDone = step.ordinal < currentStep.ordinal
                Text(
                    text = labels[step].orEmpty(),
                    style = if (isCurrent) typography.captionStrong else typography.caption,
                    color = when {
                        isCurrent -> colors.purple
                        isDone -> colors.navy
                        else -> colors.textDisabled
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(76.dp),
                )
            }
        }
    }
}

@Preview
@Composable
fun TripStepperLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Column(
                modifier = Modifier.padding(spacing.space16),
                verticalArrangement = Arrangement.spacedBy(spacing.space16),
            ) {
                TripStepper(currentStep = TripStep.OwnerOnTheWay)
                TripStepper(currentStep = TripStep.Arrived)
                TripStepper(currentStep = TripStep.InRide)
                TripStepper(currentStep = TripStep.Completed)
            }
        }
    }
}

@Preview
@Composable
fun TripStepperRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        TripStepperLtrPreview()
    }
}
