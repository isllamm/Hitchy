@file:Suppress("MagicNumber")

package com.hitchy.feature.debug.ui.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.HitchyButtonVariant
import com.hitchy.core.ui.components.HitchyTextField
import com.hitchy.core.ui.components.LocationField
import com.hitchy.core.ui.components.LocationPointType
import com.hitchy.core.ui.components.OtpField
import com.hitchy.core.ui.components.PhoneField
import com.hitchy.core.ui.components.RouteInput
import com.hitchy.core.ui.components.SearchField
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GalleryButtonsAndInputsSection(modifier: Modifier = Modifier) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current

    var textValue by remember { mutableStateOf("Cairo Festival City") }
    var phoneValue by remember { mutableStateOf("01012345678") }
    var searchQuery by remember { mutableStateOf("") }
    var otpValue by remember { mutableStateOf("123456") }
    var pickupPoint by remember { mutableStateOf("Maadi Grand Mall") }
    var dropoffPoint by remember { mutableStateOf("Smart Village Building B3") }
    var buttonLoading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.space16),
    ) {
        // Buttons Section
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
                    text = "HitchyButton Variants & States",
                    style = typography.h3,
                    color = colors.textPrimary,
                )

                Text(
                    text = "Button Variants (Enabled)",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.space8),
                    verticalArrangement = Arrangement.spacedBy(spacing.space8),
                ) {
                    HitchyButton(
                        onClick = {},
                        variant = HitchyButtonVariant.Primary,
                    ) {
                        Text("Primary")
                    }
                    HitchyButton(
                        onClick = {},
                        variant = HitchyButtonVariant.Tonal,
                    ) {
                        Text("Tonal")
                    }
                    HitchyButton(
                        onClick = {},
                        variant = HitchyButtonVariant.Outline,
                    ) {
                        Text("Outline")
                    }
                    HitchyButton(
                        onClick = {},
                        variant = HitchyButtonVariant.Text,
                    ) {
                        Text("Text")
                    }
                    HitchyButton(
                        onClick = {},
                        variant = HitchyButtonVariant.Destructive,
                    ) {
                        Text("Destructive")
                    }
                }

                Text(
                    text = "Button States & Icons",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.space8),
                    verticalArrangement = Arrangement.spacedBy(spacing.space8),
                ) {
                    HitchyButton(
                        onClick = { buttonLoading = !buttonLoading },
                        loading = buttonLoading,
                        variant = HitchyButtonVariant.Primary,
                    ) {
                        Text(if (buttonLoading) "Loading..." else "Toggle Loading")
                    }
                    HitchyButton(
                        onClick = {},
                        enabled = false,
                        variant = HitchyButtonVariant.Primary,
                    ) {
                        Text("Disabled")
                    }
                    HitchyButton(
                        onClick = {},
                        variant = HitchyButtonVariant.Primary,
                    ) {
                        Icon(
                            painter = painterResource(HitchyIcons.Car),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.size(spacing.space8))
                        Text("With Icon")
                    }
                }
            }
        }

        // Text & Phone Inputs
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
                    text = "Text Fields & Inputs",
                    style = typography.h3,
                    color = colors.textPrimary,
                )

                HitchyTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = "Destination",
                    placeholder = "Enter destination",
                    leadingIcon = {
                        Icon(
                            painter = painterResource(HitchyIcons.LocationDot),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                )

                HitchyTextField(
                    value = "",
                    onValueChange = {},
                    label = "Full Name",
                    placeholder = "Ahmed Mahmoud",
                    helperText = "As written on your national ID",
                )

                HitchyTextField(
                    value = "invalid input",
                    onValueChange = {},
                    label = "Field with Error",
                    errorText = "Please enter a valid value",
                )

                PhoneField(
                    value = phoneValue,
                    onValueChange = { phoneValue = it },
                )

                SearchField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search rides, destinations or drivers",
                )
            }
        }

        // OTP & Route Inputs
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
                    text = "OTP & Route Inputs",
                    style = typography.h3,
                    color = colors.textPrimary,
                )

                Text(
                    text = "OtpField (6 digits)",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                OtpField(
                    otpValue = otpValue,
                    onOtpChange = { otpValue = it },
                )

                Spacer(modifier = Modifier.height(spacing.space4))
                Text(
                    text = "LocationField (Pickup & Drop-off)",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                LocationField(
                    value = pickupPoint,
                    onClick = {},
                    type = LocationPointType.Pickup,
                )
                LocationField(
                    value = dropoffPoint,
                    onClick = {},
                    type = LocationPointType.DropOff,
                )

                Spacer(modifier = Modifier.height(spacing.space4))
                Text(
                    text = "RouteInput (Combined with Swap)",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                RouteInput(
                    pickupValue = pickupPoint,
                    dropOffValue = dropoffPoint,
                    onPickupClick = {},
                    onDropOffClick = {},
                    onSwap = {
                        val temp = pickupPoint
                        pickupPoint = dropoffPoint
                        dropoffPoint = temp
                    },
                )
            }
        }
    }
}
