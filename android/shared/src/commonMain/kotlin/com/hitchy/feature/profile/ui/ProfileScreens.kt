package com.hitchy.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.Gender
import com.hitchy.core.model.Role
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.components.AppRoleType
import com.hitchy.core.ui.components.Avatar
import com.hitchy.core.ui.components.AvatarSize
import com.hitchy.core.ui.components.BalanceCard
import com.hitchy.core.ui.components.BannerVariant
import com.hitchy.core.ui.components.HitchyBanner
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.HitchyButtonVariant
import com.hitchy.core.ui.components.HitchyTextField
import com.hitchy.core.ui.components.RatingDisplay
import com.hitchy.core.ui.components.RoleCard
import com.hitchy.core.ui.components.SelectChip
import com.hitchy.core.ui.components.TopBar
import com.hitchy.core.ui.message
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.profile.presentation.ProfileSetupAction
import com.hitchy.feature.profile.presentation.ProfileSetupEvent
import com.hitchy.feature.profile.presentation.ProfileSetupViewModel
import com.hitchy.feature.profile.presentation.ProfileTabAction
import com.hitchy.feature.profile.presentation.ProfileTabEvent
import com.hitchy.feature.profile.presentation.ProfileTabViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.nav_rides
import hitchy.shared.generated.resources.profile_company_label
import hitchy.shared.generated.resources.profile_company_none
import hitchy.shared.generated.resources.profile_company_other
import hitchy.shared.generated.resources.profile_company_other_placeholder
import hitchy.shared.generated.resources.profile_first_name_label
import hitchy.shared.generated.resources.profile_first_name_placeholder
import hitchy.shared.generated.resources.profile_gender_female
import hitchy.shared.generated.resources.profile_gender_label
import hitchy.shared.generated.resources.profile_gender_locked_note
import hitchy.shared.generated.resources.profile_gender_male
import hitchy.shared.generated.resources.profile_last_name_label
import hitchy.shared.generated.resources.profile_last_name_placeholder
import hitchy.shared.generated.resources.profile_save_continue
import hitchy.shared.generated.resources.profile_setup_subtitle
import hitchy.shared.generated.resources.profile_setup_title
import hitchy.shared.generated.resources.profile_sign_out
import hitchy.shared.generated.resources.profile_tab_title
import hitchy.shared.generated.resources.settings_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    languageSwitcher: LanguageSwitcher = koinInject(),
    viewModel: ProfileSetupViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val spacing = HitchyTheme.spacing
    val currentLanguage: AppLanguage = languageSwitcher.getCurrentLanguage()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileSetupEvent.Completed -> onCompleted()
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(title = stringResource(Res.string.profile_setup_title))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(spacing.screenPadding),
                verticalArrangement = Arrangement.spacedBy(spacing.space16),
            ) {
                Text(
                    text = stringResource(Res.string.profile_setup_subtitle),
                    style = typography.body,
                    color = colors.textSecondary,
                )

                val error = state.error
                if (error != null) {
                    HitchyBanner(
                        text = stringResource(error.message()),
                        variant = BannerVariant.Error,
                    )
                }

                HitchyTextField(
                    value = state.firstName,
                    onValueChange = { viewModel.onAction(ProfileSetupAction.UpdateFirstName(it)) },
                    label = stringResource(Res.string.profile_first_name_label),
                    placeholder = stringResource(Res.string.profile_first_name_placeholder),
                    enabled = !state.isSaving,
                )

                HitchyTextField(
                    value = state.lastName,
                    onValueChange = { viewModel.onAction(ProfileSetupAction.UpdateLastName(it)) },
                    label = stringResource(Res.string.profile_last_name_label),
                    placeholder = stringResource(Res.string.profile_last_name_placeholder),
                    enabled = !state.isSaving,
                )

                Column(verticalArrangement = Arrangement.spacedBy(spacing.space8)) {
                    Text(
                        text = stringResource(Res.string.profile_gender_label),
                        style = typography.captionStrong,
                        color = colors.textPrimary,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.space12)) {
                        SelectChip(
                            label = stringResource(Res.string.profile_gender_male),
                            selected = state.gender == Gender.MALE,
                            onClick = { viewModel.onAction(ProfileSetupAction.SelectGender(Gender.MALE)) },
                            enabled = !state.isSaving,
                        )
                        SelectChip(
                            label = stringResource(Res.string.profile_gender_female),
                            selected = state.gender == Gender.FEMALE,
                            onClick = { viewModel.onAction(ProfileSetupAction.SelectGender(Gender.FEMALE)) },
                            enabled = !state.isSaving,
                        )
                    }
                    Text(
                        text = stringResource(Res.string.profile_gender_locked_note),
                        style = typography.caption,
                        color = colors.textSecondary,
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(spacing.space8)) {
                    Text(
                        text = stringResource(Res.string.profile_company_label),
                        style = typography.captionStrong,
                        color = colors.textPrimary,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(spacing.space8),
                        verticalArrangement = Arrangement.spacedBy(spacing.space8),
                    ) {
                        SelectChip(
                            label = stringResource(Res.string.profile_company_none),
                            selected = state.selectedCompanyId == null && !state.isOtherCompany,
                            onClick = { viewModel.onAction(ProfileSetupAction.SelectCompany(null)) },
                            enabled = !state.isSaving,
                        )
                        state.companies.forEach { company ->
                            SelectChip(
                                label = company.name.resolve(currentLanguage),
                                selected = state.selectedCompanyId == company.id && !state.isOtherCompany,
                                onClick = {
                                    viewModel.onAction(ProfileSetupAction.SelectCompany(company.id))
                                },
                                enabled = !state.isSaving,
                            )
                        }
                        SelectChip(
                            label = stringResource(Res.string.profile_company_other),
                            selected = state.isOtherCompany,
                            onClick = { viewModel.onAction(ProfileSetupAction.SelectOtherCompany) },
                            enabled = !state.isSaving,
                        )
                    }
                    if (state.isOtherCompany) {
                        HitchyTextField(
                            value = state.companyOther,
                            onValueChange = { viewModel.onAction(ProfileSetupAction.UpdateCompanyOther(it)) },
                            placeholder = stringResource(Res.string.profile_company_other_placeholder),
                            enabled = !state.isSaving,
                        )
                    }
                }

                HitchyButton(
                    onClick = { viewModel.onAction(ProfileSetupAction.Submit) },
                    enabled = state.canSubmit && !state.isSaving,
                    loading = state.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(Res.string.profile_save_continue))
                }
            }
        }
    }
}

@Composable
fun ProfileTabContent(
    onOpenMyRides: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHostApplication: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileTabViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val spacing = HitchyTheme.spacing

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileTabEvent.NavigateToHostApplication -> onOpenHostApplication()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(spacing.space16),
    ) {
        Text(
            text = stringResource(Res.string.profile_tab_title),
            style = typography.h1,
            color = colors.navy,
        )

        val profile = state.profile
        if (profile != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.space16),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Avatar(
                    initials = profile.firstName.take(1).uppercase(),
                    size = AvatarSize.LG,
                )
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space4)) {
                    Text(
                        text = "${profile.firstName} ${profile.lastName}",
                        style = typography.h2,
                        color = colors.navy,
                    )
                    val ratingAvg = profile.rider.ratingAvg
                    if (ratingAvg != null) {
                        RatingDisplay(
                            rating = ratingAvg,
                            rideCount = profile.rider.ratingCount,
                        )
                    }
                }
            }
        }

        val error = state.error
        if (error != null) {
            HitchyBanner(
                text = stringResource(error.message()),
                variant = BannerVariant.Error,
                onDismiss = { viewModel.onAction(ProfileTabAction.DismissError) },
            )
        }

        // Real wallet balance from users/{uid}/private/wallet.balanceEgp
        BalanceCard(
            creditsEgp = state.walletBalanceEgp,
            points = profile?.pointsBalance ?: 0,
        )

        // Role switch cards
        val currentRole = profile?.activeRole ?: Role.RIDER
        RoleCard(
            role = AppRoleType.Rider,
            selected = currentRole == Role.RIDER,
            onClick = {
                if (currentRole != Role.RIDER && !state.isSwitchingRole) {
                    viewModel.onAction(ProfileTabAction.SwitchRole(Role.RIDER))
                }
            },
        )
        RoleCard(
            role = AppRoleType.Host,
            selected = currentRole == Role.HOST,
            onClick = {
                if (currentRole != Role.HOST && !state.isSwitchingRole) {
                    viewModel.onAction(ProfileTabAction.SwitchRole(Role.HOST))
                }
            },
        )

        HitchyButton(
            onClick = onOpenMyRides,
            variant = HitchyButtonVariant.Tonal,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(Res.string.nav_rides))
        }

        HitchyButton(
            onClick = onOpenSettings,
            variant = HitchyButtonVariant.Outline,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(Res.string.settings_title))
        }

        HitchyButton(
            onClick = { viewModel.onAction(ProfileTabAction.SignOut) },
            variant = HitchyButtonVariant.Destructive,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(Res.string.profile_sign_out))
        }
    }
}
