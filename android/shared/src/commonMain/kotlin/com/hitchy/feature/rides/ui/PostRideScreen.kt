package com.hitchy.feature.rides.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.platform.Sharer
import com.hitchy.core.ui.ErrorText
import com.hitchy.core.ui.StepBackHandler
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.booking.ui.ErrorToast
import com.hitchy.feature.profile.domain.ProfileRepository
import com.hitchy.feature.rides.presentation.PostRideAction
import com.hitchy.feature.rides.presentation.PostRideEvent
import com.hitchy.feature.rides.presentation.PostRideState
import com.hitchy.feature.rides.presentation.PostRideViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.offer_continue
import hitchy.shared.generated.resources.offer_go_to_rides
import hitchy.shared.generated.resources.offer_invite_text
import hitchy.shared.generated.resources.offer_publish
import hitchy.shared.generated.resources.offer_step_review
import hitchy.shared.generated.resources.offer_step_route
import hitchy.shared.generated.resources.offer_title_review
import hitchy.shared.generated.resources.offer_title_route
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private const val STEP_ROUTE = 1
private const val STEP_REVIEW = 2
private const val STEP_LIVE = 3
private const val HALF = 0.5f

/**
 * Offer a ride as Figma's owner steps: D2 route and schedule, D4 price and review (D3 ride vibe is
 * dropped), then D5 "Your commute is live". The view model and its rules are unchanged; the steps only
 * split the same form.
 */
@Composable
fun PostRideScreen(
    onBack: () -> Unit,
    onOpenRoutePicker: (String) -> Unit,
    onNavigateToRideDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
    onOpenMyRides: (() -> Unit)? = null,
    viewModel: PostRideViewModel = koinViewModel(),
    languageSwitcher: LanguageSwitcher = koinInject(),
    sharer: Sharer = koinInject(),
    profileRepository: ProfileRepository = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val language = languageSwitcher.getCurrentLanguage()
    val me by profileRepository.observeMe().collectAsStateWithLifecycle(initialValue = null)
    var step by rememberSaveable { mutableIntStateOf(STEP_ROUTE) }
    var liveRideId by rememberSaveable { mutableStateOf<String?>(null) }
    val inviteCode = me?.referralCode.orEmpty()
    val inviteText = remember(inviteCode) { mutableStateOf("") }

    LaunchedEffect(inviteCode) {
        if (inviteCode.isNotBlank()) inviteText.value = getString(Res.string.offer_invite_text, inviteCode)
    }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                PostRideEvent.ShowReview -> step = STEP_REVIEW

                is PostRideEvent.NavigateToRideDetails -> {
                    liveRideId = event.rideId
                    step = STEP_LIVE
                }
            }
        }
    }
    StepBackHandler(enabled = step == STEP_REVIEW) { step = STEP_ROUTE }

    // One scroll position for all three steps: a step opened while the last one was scrolled down started part
    // way down, so the top of "Your commute is live" was off screen. Each step now opens at its top.
    // Coming back from the route picker keeps the place, since the step is the same.
    val scrollState = rememberScrollState()
    var scrolledStep by rememberSaveable { mutableIntStateOf(step) }
    LaunchedEffect(step) {
        if (step != scrolledStep) {
            scrollState.scrollTo(0)
            scrolledStep = step
        }
    }

    val openRides = { onOpenMyRides?.invoke() ?: liveRideId?.let(onNavigateToRideDetails) ?: onBack() }
    StepBackHandler(enabled = step == STEP_LIVE) { openRides() }

    Column(modifier = modifier.fillMaxSize().background(HitchyTheme.colors.white)) {
        when (step) {
            STEP_ROUTE -> OfferTopBar(stringResource(Res.string.offer_title_route), onBack)
            STEP_REVIEW -> OfferTopBar(stringResource(Res.string.offer_title_review), onBack = { step = STEP_ROUTE })
            else -> Column(Modifier.statusBarsPadding()) {}
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(start = 16.dp, end = 16.dp, top = if (step == STEP_LIVE) 32.dp else 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (step) {
                STEP_ROUTE -> {
                    OfferStepHeader(stringResource(Res.string.offer_step_route), HALF)
                    OfferRouteStep(state, language, onOpenRoutePicker, viewModel::onAction)
                }

                STEP_REVIEW -> {
                    OfferStepHeader(stringResource(Res.string.offer_step_review), 1f)
                    OfferReviewStep(state, language, onEdit = { step = STEP_ROUTE }, onAction = viewModel::onAction)
                }

                else -> OfferLiveContent(
                    state = state,
                    language = language,
                    onShareInvite = inviteText.value
                        .takeIf { it.isNotBlank() }
                        ?.let { text -> { sharer.shareText(text) } },
                )
            }
        }
        OfferFooter {
            OfferErrors(state, viewModel::onAction)
            when (step) {
                STEP_ROUTE -> MainButton(stringResource(Res.string.offer_continue), loading = false) {
                    viewModel.onAction(PostRideAction.Continue)
                }

                STEP_REVIEW -> MainButton(stringResource(Res.string.offer_publish), loading = state.isSubmitting) {
                    viewModel.onAction(PostRideAction.Submit)
                }

                else -> MainButton(stringResource(Res.string.offer_go_to_rides), loading = false, onClick = openRides)
            }
        }
    }
}

@Composable
private fun MainButton(text: String, loading: Boolean, onClick: () -> Unit) {
    HitchyButton(onClick = onClick, loading = loading, modifier = Modifier.fillMaxWidth()) {
        Text(text = text)
    }
}

/** The route checks and server errors, as Figma's red toast over the main button. */
@Composable
private fun OfferErrors(state: PostRideState, onAction: (PostRideAction) -> Unit) {
    val validationError = state.validationError
    if (validationError != null) {
        ErrorToast(text = stringResource(validationError), onDismiss = { onAction(PostRideAction.DismissError) })
    }
    val error = state.error
    if (error != null) {
        ErrorText(error = error, style = HitchyTheme.typography.caption, color = HitchyTheme.colors.error)
    }
}
