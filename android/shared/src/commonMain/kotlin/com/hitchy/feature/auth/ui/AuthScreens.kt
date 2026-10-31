package com.hitchy.feature.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.Role
import com.hitchy.core.ui.components.AppRoleType
import com.hitchy.core.ui.components.BackButton
import com.hitchy.core.ui.components.BackButtonStyle
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.IdentityField
import com.hitchy.core.ui.components.OtpField
import com.hitchy.core.ui.components.PhoneField
import com.hitchy.core.ui.components.RoleCard
import com.hitchy.core.ui.figmaBottom
import com.hitchy.core.ui.figmaTop
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.message
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.withLinks
import com.hitchy.feature.auth.domain.SignUpNameHolder
import com.hitchy.feature.auth.presentation.OtpVerifyAction
import com.hitchy.feature.auth.presentation.OtpVerifyEvent
import com.hitchy.feature.auth.presentation.OtpVerifyViewModel
import com.hitchy.feature.auth.presentation.PhoneEntryAction
import com.hitchy.feature.auth.presentation.PhoneEntryEvent
import com.hitchy.feature.auth.presentation.PhoneEntryViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.img_brand_logo
import hitchy.shared.generated.resources.img_onboarding_1
import hitchy.shared.generated.resources.img_onboarding_2
import hitchy.shared.generated.resources.img_onboarding_3
import hitchy.shared.generated.resources.img_signin_logo
import hitchy.shared.generated.resources.onboarding_body_1
import hitchy.shared.generated.resources.onboarding_body_2
import hitchy.shared.generated.resources.onboarding_body_3
import hitchy.shared.generated.resources.onboarding_image_description
import hitchy.shared.generated.resources.onboarding_next
import hitchy.shared.generated.resources.onboarding_title_1
import hitchy.shared.generated.resources.onboarding_title_2
import hitchy.shared.generated.resources.onboarding_title_3
import hitchy.shared.generated.resources.otp_change_number
import hitchy.shared.generated.resources.otp_expired_alert_body
import hitchy.shared.generated.resources.otp_expired_alert_title
import hitchy.shared.generated.resources.otp_expired_body
import hitchy.shared.generated.resources.otp_expired_title
import hitchy.shared.generated.resources.otp_resend_in
import hitchy.shared.generated.resources.otp_resend_now
import hitchy.shared.generated.resources.otp_send_new_code
import hitchy.shared.generated.resources.otp_title
import hitchy.shared.generated.resources.otp_verify_button
import hitchy.shared.generated.resources.otp_verify_subtitle
import hitchy.shared.generated.resources.phone_entry_create_account
import hitchy.shared.generated.resources.phone_entry_create_account_link
import hitchy.shared.generated.resources.phone_entry_privacy_link
import hitchy.shared.generated.resources.phone_entry_privacy_note
import hitchy.shared.generated.resources.phone_entry_subtitle
import hitchy.shared.generated.resources.phone_entry_terms
import hitchy.shared.generated.resources.phone_entry_terms_link
import hitchy.shared.generated.resources.phone_entry_title
import hitchy.shared.generated.resources.signup_have_account
import hitchy.shared.generated.resources.signup_login_link
import hitchy.shared.generated.resources.signup_name_label
import hitchy.shared.generated.resources.signup_name_placeholder
import hitchy.shared.generated.resources.signup_subtitle
import hitchy.shared.generated.resources.signup_title
import hitchy.shared.generated.resources.splash_logo_description
import hitchy.shared.generated.resources.welcome_continue
import hitchy.shared.generated.resources.welcome_safety_note
import hitchy.shared.generated.resources.welcome_subtitle
import hitchy.shared.generated.resources.welcome_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Figma node 676:9464. The gradient logo, 300x120, centred on white. Shown while
 * [com.hitchy.feature.auth.presentation.RootViewModel] decides where to send the user.
 */
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = HitchyTheme.colors.white,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.img_brand_logo),
                contentDescription = stringResource(Res.string.splash_logo_description),
                modifier = Modifier.width(300.dp).height(120.dp),
            )
        }
    }
}

/**
 * Figma nodes 676:9471, 676:9488 and 676:9505. A full-bleed photo under a 401dp scrim,
 * with the copy, the page dots and the 32dp gradient next button pinned to the bottom.
 */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val spacing = HitchyTheme.spacing
    var currentPage by remember { mutableIntStateOf(0) }

    val photos = listOf(
        Res.drawable.img_onboarding_1,
        Res.drawable.img_onboarding_2,
        Res.drawable.img_onboarding_3,
    )
    val titles = listOf(
        stringResource(Res.string.onboarding_title_1),
        stringResource(Res.string.onboarding_title_2),
        stringResource(Res.string.onboarding_title_3),
    )
    val bodies = listOf(
        stringResource(Res.string.onboarding_body_1),
        stringResource(Res.string.onboarding_body_2),
        stringResource(Res.string.onboarding_body_3),
    )

    Box(modifier = modifier.fillMaxSize().background(colors.scrim)) {
        Image(
            painter = painterResource(photos[currentPage]),
            contentDescription = stringResource(Res.string.onboarding_image_description),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SCRIM_HEIGHT)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        SCRIM_MID_STOP to colors.scrim.copy(alpha = SCRIM_MID_ALPHA),
                        1f to colors.scrim,
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = spacing.space16)
                .padding(bottom = figmaBottom(ONBOARDING_BOTTOM)),
            verticalArrangement = Arrangement.spacedBy(35.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.space12),
            ) {
                Text(
                    text = titles[currentPage],
                    style = typography.h1,
                    color = colors.white,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = bodies[currentPage],
                    style = typography.bodyLargeStrong,
                    color = colors.white,
                    textAlign = TextAlign.Center,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    repeat(titles.size) { index ->
                        val active = index == currentPage
                        Box(
                            modifier = Modifier
                                .width(if (active) 24.dp else 6.dp)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(if (active) colors.purple else colors.white),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.brandGradient)
                        .clickable {
                            if (currentPage < titles.lastIndex) currentPage += 1 else onFinish()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(HitchyIcons.ArrowForward),
                        contentDescription = stringResource(Res.string.onboarding_next),
                        tint = colors.white,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    }
}

/**
 * Figma node 676:9522. Brand logo, title and description, the two role cards, the safety
 * line, then Continue 174dp below.
 */
@Composable
fun WelcomeScreen(
    onContinueToPhone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val spacing = HitchyTheme.spacing
    var selectedRole by remember { mutableStateOf(Role.RIDER) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.white,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.space16)
                .padding(top = figmaTop(WELCOME_TOP), bottom = figmaBottom(WELCOME_BOTTOM)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.space24),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.space32),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(spacing.space32),
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.img_brand_logo),
                            contentDescription = stringResource(Res.string.splash_logo_description),
                            modifier = Modifier.width(190.dp).height(76.dp),
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(spacing.space8),
                        ) {
                            Text(
                                text = stringResource(Res.string.welcome_title),
                                style = typography.h1,
                                color = colors.navy,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = stringResource(Res.string.welcome_subtitle),
                                style = typography.body,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(spacing.space16)) {
                        RoleCard(
                            role = AppRoleType.Host,
                            selected = selectedRole == Role.HOST,
                            onClick = { selectedRole = Role.HOST },
                        )
                        RoleCard(
                            role = AppRoleType.Rider,
                            selected = selectedRole == Role.RIDER,
                            onClick = { selectedRole = Role.RIDER },
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.space8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(HitchyIcons.ShieldCheck),
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(Res.string.welcome_safety_note),
                        style = typography.caption,
                        color = colors.textSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(WELCOME_BUTTON_GAP))

            HitchyButton(
                onClick = onContinueToPhone,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(Res.string.welcome_continue))
            }
        }
    }
}

/**
 * Figma nodes 676:9566 (Sign in) and 676:9612 (Invalid number). The error state is the
 * same screen with the field's stroke and helper line turned red.
 */
@Composable
fun PhoneEntryScreen(
    onBack: () -> Unit,
    onNavigateToOtp: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PhoneEntryViewModel = koinViewModel(),
    signUpNameHolder: SignUpNameHolder = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Figma 828:15720 Sign in and 828:15816 Sign up share this screen; the link under the notice switches.
    var signUp by rememberSaveable { mutableStateOf(false) }
    var fullName by rememberSaveable { mutableStateOf(signUpNameHolder.fullName) }
    val subtitleRes = if (signUp) Res.string.signup_subtitle else Res.string.phone_entry_subtitle
    val linkRes = if (signUp) Res.string.signup_login_link else Res.string.phone_entry_create_account_link
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val spacing = HitchyTheme.spacing

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PhoneEntryEvent.NavigateToOtp -> onNavigateToOtp(event.phoneE164)
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.white,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.space16)
                    .padding(top = figmaTop(AUTH_TOP)),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.space16),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(spacing.space24),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(spacing.space16),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(spacing.space32),
                            ) {
                                Column {
                                    BackButton(
                                        onClick = { if (signUp) signUp = false else onBack() },
                                        style = BackButtonStyle.OutlinedCircle,
                                        modifier = Modifier.size(44.dp),
                                    )
                                    // Figma: the 170x68 logo 13dp under the back button, the heading 17dp under it.
                                    Image(
                                        painter = painterResource(Res.drawable.img_signin_logo),
                                        contentDescription = null,
                                        modifier = Modifier.padding(top = 13.dp).size(width = 170.dp, height = 68.dp),
                                    )
                                    Column(
                                        modifier = Modifier.padding(top = 17.dp),
                                        verticalArrangement = Arrangement.spacedBy(spacing.space8),
                                    ) {
                                        Text(
                                            text = stringResource(
                                                if (signUp) Res.string.signup_title else Res.string.phone_entry_title,
                                            ),
                                            style = typography.h1,
                                            color = colors.navy,
                                        )
                                        Text(
                                            text = stringResource(
                                                subtitleRes,
                                            ),
                                            style = typography.body,
                                            color = colors.textSecondary,
                                        )
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(spacing.space16)) {
                                    if (signUp) {
                                        IdentityField(
                                            value = fullName,
                                            onValueChange = {
                                                fullName = it
                                                signUpNameHolder.fullName = it
                                            },
                                            label = stringResource(Res.string.signup_name_label),
                                            placeholder = stringResource(Res.string.signup_name_placeholder),
                                        )
                                    }

                                    PhoneField(
                                        value = state.phoneInput,
                                        onValueChange = { viewModel.onAction(PhoneEntryAction.UpdatePhone(it)) },
                                        // The field's own error line carries Figma's copy for a bad number.
                                        isError = state.error != null,
                                    )
                                }
                            }

                            HitchyButton(
                                onClick = { viewModel.onAction(PhoneEntryAction.Submit) },
                                // Figma's Invalid number frame keeps Continue active: Submit shows the error.
                                enabled = state.phoneInput.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(text = stringResource(Res.string.welcome_continue))
                            }
                        }

                        PrivacyNotice(text = stringResource(Res.string.phone_entry_privacy_note))
                    }

                    Text(
                        text = withLinks(
                            template = stringResource(
                                if (signUp) Res.string.signup_have_account else Res.string.phone_entry_create_account,
                            ),
                            links = listOf(
                                stringResource(
                                    linkRes,
                                ),
                            ),
                            linkStyle = SpanStyle(fontWeight = FontWeight.Bold, color = colors.violet),
                        ),
                        style = typography.body,
                        color = colors.navy,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { signUp = !signUp },
                    )
                }
            }

            // Figma: 311dp wide, so it wraps to two lines, 44dp above the screen bottom.
            Text(
                text = withLinks(
                    template = stringResource(Res.string.phone_entry_terms),
                    links = listOf(
                        stringResource(Res.string.phone_entry_terms_link),
                        stringResource(Res.string.phone_entry_privacy_link),
                    ),
                    linkStyle = SpanStyle(fontWeight = FontWeight.Bold, color = colors.violet),
                ),
                style = typography.caption,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(TERMS_WIDTH)
                    .padding(bottom = figmaBottom(TERMS_BOTTOM)),
            )
        }
    }
}

/**
 * Figma nodes 676:9658 (Verification code) and 676:9705 (Wrong code). A wrong code turns
 * the six boxes red and adds the error line under them.
 */
@Composable
fun OtpVerifyScreen(
    phoneE164: String,
    onBack: () -> Unit,
    onVerified: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OtpVerifyViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val spacing = HitchyTheme.spacing
    val error = state.error
    // Figma paints the boxes red for a wrong code only; a failed send keeps them plain.
    val wrongCode = error != null && state.code.length == OTP_LENGTH
    // Figma frame 384:4313 "Code Expired Screen": the same screen with its own title, red boxes and an alert.
    val expired = state.isCodeExpired

    LaunchedEffect(phoneE164) {
        viewModel.onAction(OtpVerifyAction.Init(phoneE164))
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                OtpVerifyEvent.Verified -> onVerified()
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.white,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.space16)
                .padding(top = figmaTop(AUTH_TOP), bottom = spacing.space24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.space24),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing.space16),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.space40),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.space24)) {
                        BackButton(
                            onClick = onBack,
                            style = BackButtonStyle.OutlinedCircle,
                            modifier = Modifier.size(44.dp),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.space8)) {
                            Text(
                                text = stringResource(
                                    if (expired) Res.string.otp_expired_title else Res.string.otp_title,
                                ),
                                style = typography.h1,
                                color = colors.navy,
                            )
                            Text(
                                text = if (expired) {
                                    stringResource(Res.string.otp_expired_body)
                                } else {
                                    stringResource(Res.string.otp_verify_subtitle, formatEgyptPhone(phoneE164))
                                },
                                style = typography.body,
                                color = colors.textSecondary,
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(
                            if (error != null) spacing.space16 else spacing.space24,
                        ),
                    ) {
                        OtpField(
                            otpValue = state.code,
                            onOtpChange = { viewModel.onAction(OtpVerifyAction.UpdateCode(it)) },
                            isError = wrongCode || expired,
                            enabled = !state.isVerifying,
                        )

                        if (error != null && !expired) {
                            Text(
                                text = stringResource(error.message()),
                                style = typography.body,
                                color = colors.errorText,
                                textAlign = TextAlign.Center,
                            )
                        }

                        if (!expired) {
                            Text(
                                text = if (state.resendSecondsLeft > 0) {
                                    stringResource(
                                        Res.string.otp_resend_in,
                                        formatCountdown(state.resendSecondsLeft),
                                    )
                                } else {
                                    stringResource(Res.string.otp_resend_now)
                                },
                                style = typography.caption,
                                color = colors.textSecondary,
                                modifier = Modifier.clickable(enabled = state.canResend) {
                                    viewModel.onAction(OtpVerifyAction.Resend)
                                },
                            )
                        }
                    }
                }

                if (expired) {
                    ExpiredAlert(modifier = Modifier.padding(vertical = 8.dp))
                    HitchyButton(
                        onClick = { viewModel.onAction(OtpVerifyAction.Resend) },
                        enabled = state.canResend,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(Res.string.otp_send_new_code))
                    }
                } else {
                    HitchyButton(
                        onClick = { viewModel.onAction(OtpVerifyAction.Submit) },
                        enabled = state.code.length == OTP_LENGTH && !state.isVerifying,
                        loading = state.isVerifying,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(Res.string.otp_verify_button))
                    }
                }
            }

            Text(
                text = stringResource(Res.string.otp_change_number),
                style = typography.title,
                color = colors.purple,
                modifier = Modifier.clickable(enabled = !state.isVerifying, onClick = onBack),
            )
        }
    }
}

/** Figma's 361x70 r=16 #FEE2E2 alert on the Code Expired screen: an 18dp mark and two lines. */
@Composable
private fun ExpiredAlert(modifier: Modifier = Modifier) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.errorBg)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(HitchyIcons.CloseSmall),
            contentDescription = null,
            tint = colors.errorText,
            modifier = Modifier.size(18.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = stringResource(Res.string.otp_expired_alert_title),
                style = typography.captionStrong,
                color = colors.errorText,
            )
            Text(
                text = stringResource(Res.string.otp_expired_alert_body),
                style = typography.caption,
                color = colors.errorText,
            )
        }
    }
}

/** Figma's 361x42 r=12 #DBEAFE strip with a shield and one line of #1D4ED8 text. */
@Composable
private fun PrivacyNotice(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val spacing = HitchyTheme.spacing

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.infoBg)
            .padding(spacing.space12),
        horizontalArrangement = Arrangement.spacedBy(spacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(HitchyIcons.ShieldCheck),
            contentDescription = null,
            tint = colors.infoText,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            style = typography.caption,
            color = colors.infoText,
        )
    }
}

/** "+201012345678" as Figma writes it: "+20 10 1234 5678". Anything else is shown as given. */
private fun formatEgyptPhone(e164: String): String {
    val national = e164.removePrefix("+20")
    if (national.length != EGYPT_NATIONAL_DIGITS || national == e164) return e164
    val (first, second) = PHONE_GROUP_ENDS
    return "+20 ${national.take(first)} ${national.substring(first, second)} ${national.substring(second)}"
}

/** "00:30", the shape Figma shows on the verification screens. */
private fun formatCountdown(totalSeconds: Int): String {
    val minutes = totalSeconds / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

private val SCRIM_HEIGHT = 401.dp

// Figma lists the scrim as three #0F0F0F stops; the alphas fade it in from clear over the photo.
private const val SCRIM_MID_STOP = 0.45f
private const val SCRIM_MID_ALPHA = 0.55f

// Offsets from the screen edges, read from the Figma frames (393x852).
private val ONBOARDING_BOTTOM = 64.dp
private val WELCOME_TOP = 132.dp
private val WELCOME_BOTTOM = 38.dp
private val WELCOME_BUTTON_GAP = 174.dp
private val AUTH_TOP = 60.dp
private val TERMS_BOTTOM = 44.dp
private val TERMS_WIDTH = 311.dp

private const val OTP_LENGTH = 6
private const val EGYPT_NATIONAL_DIGITS = 10

// "10 1234 5678": the groups end after the 2nd and 6th digit.
private val PHONE_GROUP_ENDS = 2 to 6
private const val SECONDS_PER_MINUTE = 60
