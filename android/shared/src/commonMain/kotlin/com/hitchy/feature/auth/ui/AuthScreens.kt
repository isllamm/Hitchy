package com.hitchy.feature.auth.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.Role
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.components.AppRoleType
import com.hitchy.core.ui.components.BannerVariant
import com.hitchy.core.ui.components.HitchyBanner
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.HitchyButtonVariant
import com.hitchy.core.ui.components.OtpField
import com.hitchy.core.ui.components.PhoneField
import com.hitchy.core.ui.components.RoleCard
import com.hitchy.core.ui.components.SelectChip
import com.hitchy.core.ui.components.TopBar
import com.hitchy.core.ui.message
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.auth.presentation.OtpVerifyAction
import com.hitchy.feature.auth.presentation.OtpVerifyEvent
import com.hitchy.feature.auth.presentation.OtpVerifyViewModel
import com.hitchy.feature.auth.presentation.PhoneEntryAction
import com.hitchy.feature.auth.presentation.PhoneEntryEvent
import com.hitchy.feature.auth.presentation.PhoneEntryViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.action_next
import hitchy.shared.generated.resources.language_arabic
import hitchy.shared.generated.resources.language_english
import hitchy.shared.generated.resources.onboarding_body_1
import hitchy.shared.generated.resources.onboarding_body_2
import hitchy.shared.generated.resources.onboarding_body_3
import hitchy.shared.generated.resources.onboarding_get_started
import hitchy.shared.generated.resources.onboarding_skip
import hitchy.shared.generated.resources.onboarding_title_1
import hitchy.shared.generated.resources.onboarding_title_2
import hitchy.shared.generated.resources.onboarding_title_3
import hitchy.shared.generated.resources.otp_change_number
import hitchy.shared.generated.resources.otp_resend_in
import hitchy.shared.generated.resources.otp_resend_now
import hitchy.shared.generated.resources.otp_title
import hitchy.shared.generated.resources.otp_verify_button
import hitchy.shared.generated.resources.otp_verify_subtitle
import hitchy.shared.generated.resources.phone_entry_privacy_note
import hitchy.shared.generated.resources.phone_entry_title
import hitchy.shared.generated.resources.welcome_continue
import hitchy.shared.generated.resources.welcome_how_it_works
import hitchy.shared.generated.resources.welcome_subtitle
import hitchy.shared.generated.resources.welcome_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val spacing = HitchyTheme.spacing
    var currentPage by remember { mutableIntStateOf(0) }

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

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                HitchyButton(
                    onClick = onFinish,
                    variant = HitchyButtonVariant.Text,
                ) {
                    Text(text = stringResource(Res.string.onboarding_skip))
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.space16),
                modifier = Modifier.padding(horizontal = spacing.space16),
            ) {
                Text(
                    text = titles[currentPage],
                    style = typography.h1,
                    color = colors.navy,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = bodies[currentPage],
                    style = typography.body,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.space8),
                    modifier = Modifier.padding(top = spacing.space16),
                ) {
                    repeat(titles.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentPage) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (index == currentPage) colors.purple else colors.border),
                        )
                    }
                }
            }

            HitchyButton(
                onClick = {
                    if (currentPage < titles.lastIndex) {
                        currentPage += 1
                    } else {
                        onFinish()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                val label = if (currentPage < titles.lastIndex) {
                    stringResource(Res.string.action_next)
                } else {
                    stringResource(Res.string.onboarding_get_started)
                }
                Text(text = label)
            }
        }
    }
}

@Composable
fun WelcomeScreen(
    onContinueToPhone: () -> Unit,
    onOpenOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
    languageSwitcher: LanguageSwitcher = koinInject(),
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val spacing = HitchyTheme.spacing
    var selectedRole by remember { mutableStateOf(Role.RIDER) }
    var currentLang by remember { mutableStateOf(languageSwitcher.getCurrentLanguage()) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(spacing.screenPadding),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.space8)) {
                    SelectChip(
                        label = stringResource(Res.string.language_arabic),
                        selected = currentLang == AppLanguage.ARABIC,
                        onClick = {
                            currentLang = AppLanguage.ARABIC
                            languageSwitcher.setLanguage(AppLanguage.ARABIC)
                        },
                    )
                    SelectChip(
                        label = stringResource(Res.string.language_english),
                        selected = currentLang == AppLanguage.ENGLISH,
                        onClick = {
                            currentLang = AppLanguage.ENGLISH
                            languageSwitcher.setLanguage(AppLanguage.ENGLISH)
                        },
                    )
                }
                HitchyButton(
                    onClick = onOpenOnboarding,
                    variant = HitchyButtonVariant.Text,
                ) {
                    Text(text = stringResource(Res.string.welcome_how_it_works))
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(spacing.space16),
                modifier = Modifier.padding(vertical = spacing.space24),
            ) {
                Text(
                    text = stringResource(Res.string.welcome_title),
                    style = typography.display,
                    color = colors.navy,
                )
                Text(
                    text = stringResource(Res.string.welcome_subtitle),
                    style = typography.body,
                    color = colors.textSecondary,
                )
                Spacer(modifier = Modifier.height(spacing.space8))
                RoleCard(
                    role = AppRoleType.Rider,
                    selected = selectedRole == Role.RIDER,
                    onClick = { selectedRole = Role.RIDER },
                )
                RoleCard(
                    role = AppRoleType.Host,
                    selected = selectedRole == Role.HOST,
                    onClick = { selectedRole = Role.HOST },
                )
            }

            HitchyButton(
                onClick = onContinueToPhone,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(Res.string.welcome_continue))
            }
        }
    }
}

@Composable
fun PhoneEntryScreen(
    onBack: () -> Unit,
    onNavigateToOtp: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PhoneEntryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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
        color = colors.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                title = stringResource(Res.string.phone_entry_title),
                onBackClick = onBack,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spacing.screenPadding),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space16)) {
                    PhoneField(
                        value = state.phoneInput,
                        onValueChange = { viewModel.onAction(PhoneEntryAction.UpdatePhone(it)) },
                        isError = state.error != null,
                        errorMessage = state.error?.let { stringResource(it.message()) },
                    )
                    Text(
                        text = stringResource(Res.string.phone_entry_privacy_note),
                        style = typography.caption,
                        color = colors.textSecondary,
                    )
                }

                HitchyButton(
                    onClick = { viewModel.onAction(PhoneEntryAction.Submit) },
                    enabled = state.isValid,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(Res.string.welcome_continue))
                }
            }
        }
    }
}

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
        color = colors.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                title = stringResource(Res.string.otp_title),
                onBackClick = onBack,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spacing.screenPadding),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(spacing.space16),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(Res.string.otp_verify_subtitle, phoneE164),
                        style = typography.body,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )

                    OtpField(
                        otpValue = state.code,
                        onOtpChange = { viewModel.onAction(OtpVerifyAction.UpdateCode(it)) },
                        isError = state.error != null,
                        enabled = !state.isVerifying,
                    )

                    val error = state.error
                    if (error != null) {
                        HitchyBanner(
                            text = stringResource(error.message()),
                            variant = BannerVariant.Error,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        HitchyButton(
                            onClick = { viewModel.onAction(OtpVerifyAction.Resend) },
                            variant = HitchyButtonVariant.Text,
                            enabled = state.canResend,
                        ) {
                            val resendLabel = if (state.resendSecondsLeft > 0) {
                                stringResource(Res.string.otp_resend_in, state.resendSecondsLeft)
                            } else {
                                stringResource(Res.string.otp_resend_now)
                            }
                            Text(text = resendLabel)
                        }

                        HitchyButton(
                            onClick = onBack,
                            variant = HitchyButtonVariant.Text,
                            enabled = !state.isVerifying,
                        ) {
                            Text(text = stringResource(Res.string.otp_change_number))
                        }
                    }
                }

                HitchyButton(
                    onClick = { viewModel.onAction(OtpVerifyAction.Submit) },
                    enabled = state.code.length == 6 && !state.isVerifying,
                    loading = state.isVerifying,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(Res.string.otp_verify_button))
                }
            }
        }
    }
}
