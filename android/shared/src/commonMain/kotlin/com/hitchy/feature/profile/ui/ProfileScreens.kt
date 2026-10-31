package com.hitchy.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.Gender
import com.hitchy.core.model.Role
import com.hitchy.core.model.SavedPlaceLabel
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.WhiteStatusBarIcons
import com.hitchy.core.ui.components.BannerVariant
import com.hitchy.core.ui.components.FloatingBanner
import com.hitchy.core.ui.components.HitchyBanner
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.HitchyTextField
import com.hitchy.core.ui.components.SelectChip
import com.hitchy.core.ui.components.TopBar
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.message
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.profile.presentation.ProfileSetupAction
import com.hitchy.feature.profile.presentation.ProfileSetupEvent
import com.hitchy.feature.profile.presentation.ProfileSetupViewModel
import com.hitchy.feature.profile.presentation.ProfileTabAction
import com.hitchy.feature.profile.presentation.ProfileTabEvent
import com.hitchy.feature.profile.presentation.ProfileTabViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.fig_more_about
import hitchy.shared.generated.resources.fig_more_bell
import hitchy.shared.generated.resources.fig_more_car
import hitchy.shared.generated.resources.fig_more_help
import hitchy.shared.generated.resources.fig_more_language
import hitchy.shared.generated.resources.fig_more_location
import hitchy.shared.generated.resources.fig_more_person
import hitchy.shared.generated.resources.fig_more_terms
import hitchy.shared.generated.resources.profile_about
import hitchy.shared.generated.resources.profile_company_label
import hitchy.shared.generated.resources.profile_company_none
import hitchy.shared.generated.resources.profile_company_other
import hitchy.shared.generated.resources.profile_company_other_placeholder
import hitchy.shared.generated.resources.profile_design_info_title
import hitchy.shared.generated.resources.profile_design_settings_row
import hitchy.shared.generated.resources.profile_first_name_label
import hitchy.shared.generated.resources.profile_first_name_placeholder
import hitchy.shared.generated.resources.profile_gender_female
import hitchy.shared.generated.resources.profile_gender_label
import hitchy.shared.generated.resources.profile_gender_locked_note
import hitchy.shared.generated.resources.profile_gender_male
import hitchy.shared.generated.resources.profile_info_commute
import hitchy.shared.generated.resources.profile_info_company
import hitchy.shared.generated.resources.profile_info_home_area
import hitchy.shared.generated.resources.profile_info_phone
import hitchy.shared.generated.resources.profile_language
import hitchy.shared.generated.resources.profile_last_name_label
import hitchy.shared.generated.resources.profile_last_name_placeholder
import hitchy.shared.generated.resources.profile_menu_help
import hitchy.shared.generated.resources.profile_menu_my_rides
import hitchy.shared.generated.resources.profile_menu_notifications
import hitchy.shared.generated.resources.profile_menu_saved_places
import hitchy.shared.generated.resources.profile_mode_driver
import hitchy.shared.generated.resources.profile_mode_label
import hitchy.shared.generated.resources.profile_mode_passenger
import hitchy.shared.generated.resources.profile_not_set
import hitchy.shared.generated.resources.profile_personal_info
import hitchy.shared.generated.resources.profile_rating_rides
import hitchy.shared.generated.resources.profile_save_continue
import hitchy.shared.generated.resources.profile_section_info
import hitchy.shared.generated.resources.profile_section_support
import hitchy.shared.generated.resources.profile_setup_subtitle
import hitchy.shared.generated.resources.profile_setup_title
import hitchy.shared.generated.resources.profile_sign_out
import hitchy.shared.generated.resources.profile_switch_to_owner
import hitchy.shared.generated.resources.profile_switch_to_rider
import hitchy.shared.generated.resources.profile_tab_title
import hitchy.shared.generated.resources.profile_terms
import hitchy.shared.generated.resources.rides_tab_title
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

/**
 * The Profile tab, Figma frames 398:6594 and 398:5806: a purple header, the avatar card, the info card,
 * the wallet (demo), Current mode with the Passenger / Driver switch, the menu card and Sign out.
 * Edit Profile, Saved Places, Help & Support and Travel safely have no screen yet and are not shown.
 */
@Composable
fun ProfileTabContent(
    onOpenMyRides: () -> Unit,
    onOpenHostApplication: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenNotifications: () -> Unit = {},
    viewModel: ProfileTabViewModel = koinViewModel(),
    languageSwitcher: LanguageSwitcher = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val profile = state.profile

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileTabEvent.NavigateToHostApplication -> onOpenHostApplication()
            }
        }
    }

    val currentRole = profile?.activeRole ?: Role.RIDER
    val language = languageSwitcher.getCurrentLanguage()
    WhiteStatusBarIcons()

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(colors.white)) {
            if (profile != null) {
                val stats = if (currentRole == Role.HOST) {
                    profile.host.ratingAvg to profile.host.tripsCompleted
                } else {
                    profile.rider.ratingAvg to profile.rider.tripsCompleted
                }
                MoreHeader(
                    initials = "${profile.firstName.take(1)}${profile.lastName.take(1)}".uppercase(),
                    name = "${profile.firstName} ${profile.lastName}",
                    verified = profile.host.verified,
                    // No rating and no rides yet: the line stays hidden.
                    ratingLine = if (stats.first == null && stats.second == 0) {
                        null
                    } else {
                        stringResource(Res.string.profile_rating_rides, stats.first?.toString() ?: "—", stats.second)
                    },
                    // No edit screen yet.
                    onEditProfile = null,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                MoreWalletCard(balanceEgp = state.walletBalanceEgp)
                MoreSection(title = stringResource(Res.string.profile_section_info)) {
                    // Personal Information and Saved Places have no screen yet.
                    MoreRow(
                        Res.drawable.fig_more_person,
                        stringResource(Res.string.profile_personal_info),
                        onClick = null,
                    )
                    MoreRow(Res.drawable.fig_more_car, stringResource(Res.string.profile_menu_my_rides), onOpenMyRides)
                    MoreRow(
                        Res.drawable.fig_more_location,
                        stringResource(Res.string.profile_menu_saved_places),
                        onClick = null,
                    )
                    // Not in the frame: the demo switches between rider and owner here.
                    MoreRow(
                        icon = Res.drawable.fig_more_car,
                        label = stringResource(
                            if (currentRole == Role.RIDER) {
                                Res.string.profile_switch_to_owner
                            } else {
                                Res.string.profile_switch_to_rider
                            },
                        ),
                        onClick = if (state.isSwitchingRole) {
                            null
                        } else {
                            {
                                val next = if (currentRole == Role.RIDER) Role.HOST else Role.RIDER
                                viewModel.onAction(ProfileTabAction.SwitchRole(next))
                            }
                        },
                    )
                }
                MoreSection(title = stringResource(Res.string.settings_title)) {
                    // There is no notification setting yet: the switch shows on, the row opens the inbox.
                    MoreRow(
                        icon = Res.drawable.fig_more_bell,
                        label = stringResource(Res.string.profile_menu_notifications),
                        onClick = onOpenNotifications,
                        trailing = { MoreSwitch(checked = true) },
                    )
                    MoreRow(
                        icon = Res.drawable.fig_more_language,
                        label = stringResource(Res.string.profile_language),
                        onClick = null,
                        trailing = { LanguageToggle(current = language) { languageSwitcher.setLanguage(it) } },
                    )
                }
                MoreSection(title = stringResource(Res.string.profile_section_support)) {
                    // About, Help and Terms have no screen yet.
                    MoreRow(Res.drawable.fig_more_about, stringResource(Res.string.profile_about), onClick = null)
                    MoreRow(Res.drawable.fig_more_help, stringResource(Res.string.profile_menu_help), onClick = null)
                    MoreRow(Res.drawable.fig_more_terms, stringResource(Res.string.profile_terms), onClick = null)
                }
                LogoutRow(onClick = { viewModel.onAction(ProfileTabAction.SignOut) })
            }
        }
        state.error?.let { error ->
            FloatingBanner(
                text = stringResource(error.message()),
                onDismiss = { viewModel.onAction(ProfileTabAction.DismissError) },
            )
        }
    }
}
