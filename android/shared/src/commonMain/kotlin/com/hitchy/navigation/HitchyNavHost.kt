package com.hitchy.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.hitchy.core.model.AppConfig
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.Env
import com.hitchy.core.model.Role
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.auth.presentation.RootState
import com.hitchy.feature.auth.presentation.RootViewModel
import com.hitchy.feature.auth.ui.OnboardingScreen
import com.hitchy.feature.auth.ui.OtpVerifyScreen
import com.hitchy.feature.auth.ui.PhoneEntryScreen
import com.hitchy.feature.auth.ui.SplashScreen
import com.hitchy.feature.auth.ui.WelcomeScreen
import com.hitchy.feature.booking.domain.SearchInput
import com.hitchy.feature.booking.ui.HomeScreen
import com.hitchy.feature.booking.ui.RiderRideDetailsScreen
import com.hitchy.feature.booking.ui.RiderRidesTabContent
import com.hitchy.feature.booking.ui.SearchResultsScreen
import com.hitchy.feature.debug.ui.DebugScreen
import com.hitchy.feature.debug.ui.gallery.ComponentGalleryScreen
import com.hitchy.feature.inbox.ui.NotificationsScreen
import com.hitchy.feature.places.ui.MapPointPickerScreen
import com.hitchy.feature.places.ui.RoutePickerScreen
import com.hitchy.feature.profile.ui.ProfileSetupScreen
import com.hitchy.feature.rides.ui.HostRideDetailsScreen
import com.hitchy.feature.rides.ui.PostRideScreen
import com.hitchy.feature.trip.ui.HostTripScreen
import com.hitchy.feature.trip.ui.RiderTripScreen
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.app_name
import hitchy.shared.generated.resources.language_arabic
import hitchy.shared.generated.resources.language_english
import hitchy.shared.generated.resources.placeholder_screen
import hitchy.shared.generated.resources.settings_language_label
import hitchy.shared.generated.resources.settings_title
import hitchy.shared.generated.resources.settings_version
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun RootState.toStartDestination(): Any? = when (this) {
    RootState.Loading -> null
    RootState.SignedOut -> Onboarding
    RootState.NeedsProfile -> ProfileSetup
    is RootState.Ready -> Home
}

@Composable
fun HitchyNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Any? = null,
    appConfig: AppConfig = koinInject(),
    rootViewModel: RootViewModel = koinViewModel(),
) {
    val rootState by rootViewModel.state.collectAsStateWithLifecycle()
    val targetRoot = startDestination ?: rootState.toStartDestination()
    val initialDestination = remember(targetRoot != null) { targetRoot }

    if (initialDestination == null) {
        SplashScreen(modifier = modifier)
        return
    }

    LaunchedEffect(targetRoot) {
        val dest = targetRoot ?: return@LaunchedEffect
        // After a sign-out and a new sign-in, the root is the start destination again but the screen
        // on top is still the code screen, so compare with what is shown too.
        val shown = navController.currentBackStackEntry?.destination?.hasRoute(dest::class) ?: true
        if (dest != initialDestination || rootState is RootState.SignedOut || !shown) {
            navController.navigate(dest) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = initialDestination,
        modifier = modifier,
    ) {
        composable<Welcome> {
            WelcomeScreen(
                onContinueToPhone = { navController.navigate(PhoneEntry) },
            )
        }
        composable<PhoneEntry> {
            PhoneEntryScreen(
                onBack = { navController.popBackStack() },
                onNavigateToOtp = { phoneE164 -> navController.navigate(OtpVerify(phoneE164)) },
            )
        }
        composable<OtpVerify> { backStackEntry ->
            val route = backStackEntry.toRoute<OtpVerify>()
            OtpVerifyScreen(
                phoneE164 = route.phoneE164,
                onBack = { navController.popBackStack() },
                onVerified = {
                    // RootViewModel reacts to AuthState changes automatically
                },
            )
        }
        composable<ProfileSetup> {
            ProfileSetupScreen(
                onCompleted = {
                    navController.navigate(Home) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
        composable<Home> {
            val readyState = rootState as? RootState.Ready
            HomeScreen(
                activeRole = readyState?.activeRole ?: Role.RIDER,
                onOpenNotifications = { navController.navigate(Notifications) },
                onOpenRoutePicker = { field -> navController.navigate(RoutePicker(field)) },
                onNavigateToSearchResults = { event ->
                    navController.navigate(
                        SearchResults(
                            fromZone = event.fromZone,
                            toZone = event.toZone,
                            fromPointId = event.fromPointId,
                            date = event.date,
                            time = event.time,
                            seats = event.seats,
                        ),
                    )
                },
                onOpenRiderRideDetails = { rideId -> navController.navigate(RiderRideDetails(rideId)) },
                onOpenRiderTrip = { rideId -> navController.navigate(RiderTrip(rideId)) },
                onOpenHostRideDetails = { rideId -> navController.navigate(HostRideDetails(rideId)) },
                onOpenHostTrip = { rideId -> navController.navigate(HostTrip(rideId)) },
                onOpenPostRide = { navController.navigate(PostRide) },
                onOpenHostApplication = { navController.navigate(HostApplication) },
            )
        }
        composable<Settings> {
            SettingsPlaceholderScreen(
                onNavigateToDebug = { navController.navigate(Debug) },
            )
        }
        composable<HostApplication> { PlaceholderScreen("HostApplication") }
        composable<HostStatus> { PlaceholderScreen("HostStatus") }
        composable<AddVehicle> { PlaceholderScreen("AddVehicle") }
        composable<PostRide> {
            PostRideScreen(
                onBack = { navController.popBackStack() },
                onOpenRoutePicker = { field -> navController.navigate(RoutePicker(field)) },
                onNavigateToRideDetails = { rideId ->
                    navController.navigate(HostRideDetails(rideId)) {
                        popUpTo<Home> { inclusive = false }
                    }
                },
                onOpenMyRides = {
                    navController.navigate(MyTrips) {
                        popUpTo<Home> { inclusive = false }
                    }
                },
            )
        }
        composable<MyRides> { PlaceholderScreen("MyRides") }
        composable<HostRideDetails> { backStackEntry ->
            val route = backStackEntry.toRoute<HostRideDetails>()
            HostRideDetailsScreen(
                rideId = route.rideId,
                onBack = { navController.popBackStack() },
                onOpenTrip = { rideId -> navController.navigate(HostTrip(rideId)) },
            )
        }
        composable<Search> { PlaceholderScreen("Search") }
        composable<SearchResults> { backStackEntry ->
            val route = backStackEntry.toRoute<SearchResults>()
            val input = remember(route) {
                SearchInput(
                    fromZone = route.fromZone,
                    toZone = route.toZone,
                    fromPointId = route.fromPointId,
                    date = route.date,
                    time = route.time,
                    seats = route.seats,
                )
            }
            SearchResultsScreen(
                input = input,
                onBack = { navController.popBackStack() },
                onOpenRideDetails = { rideId -> navController.navigate(RiderRideDetails(rideId)) },
            )
        }
        composable<RiderRideDetails> { backStackEntry ->
            val route = backStackEntry.toRoute<RiderRideDetails>()
            RiderRideDetailsScreen(
                rideId = route.rideId,
                onBack = { navController.popBackStack() },
                onOpenTrip = { rideId -> navController.navigate(RiderTrip(rideId)) },
            )
        }
        composable<MyTrips> {
            RiderRidesTabContent(
                onBack = { navController.popBackStack() },
                onOpenRiderRideDetails = { rideId -> navController.navigate(RiderRideDetails(rideId)) },
                onOpenRiderTrip = { rideId -> navController.navigate(RiderTrip(rideId)) },
                onOpenHostRideDetails = { rideId -> navController.navigate(HostRideDetails(rideId)) },
                onOpenHostTrip = { rideId -> navController.navigate(HostTrip(rideId)) },
            )
        }
        composable<HostTrip> { backStackEntry ->
            val route = backStackEntry.toRoute<HostTrip>()
            HostTripScreen(
                rideId = route.rideId,
                onBack = { navController.popBackStack() },
            )
        }
        composable<RiderTrip> { backStackEntry ->
            val route = backStackEntry.toRoute<RiderTrip>()
            RiderTripScreen(
                rideId = route.rideId,
                onBack = { navController.popBackStack() },
            )
        }
        composable<RateTrip> { PlaceholderScreen("RateTrip") }
        composable<Fees> { PlaceholderScreen("Fees") }
        composable<Packages> { PlaceholderScreen("Packages") }
        composable<PaymentInstructions> { PlaceholderScreen("PaymentInstructions") }
        composable<CreditHistory> { PlaceholderScreen("CreditHistory") }
        composable<Rewards> { PlaceholderScreen("Rewards") }
        composable<Invite> { PlaceholderScreen("Invite") }
        composable<Assistant> { PlaceholderScreen("Assistant") }
        composable<FixedRides> { PlaceholderScreen("FixedRides") }
        composable<CreateFixedRide> { PlaceholderScreen("CreateFixedRide") }
        composable<FixedRideDetails> { PlaceholderScreen("FixedRideDetails") }
        composable<JoinFixedRide> { PlaceholderScreen("JoinFixedRide") }
        composable<BackupOptions> { PlaceholderScreen("BackupOptions") }
        composable<EmergencyContact> { PlaceholderScreen("EmergencyContact") }
        composable<DeleteAccount> { PlaceholderScreen("DeleteAccount") }
        if (appConfig.env == Env.DEV) {
            composable<Debug> {
                DebugScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToGallery = { navController.navigate(Gallery) },
                )
            }
            composable<Gallery> {
                ComponentGalleryScreen(
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable<Onboarding> {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Welcome) {
                        popUpTo<Onboarding> { inclusive = true }
                    }
                },
            )
        }
        composable<RoutePicker> { backStackEntry ->
            val route = backStackEntry.toRoute<RoutePicker>()
            RoutePickerScreen(
                field = route.field,
                onBack = { navController.popBackStack() },
                onOpenMapPicker = { f -> navController.navigate(MapPointPicker(f)) },
                onPointSelected = { navController.popBackStack() },
            )
        }
        composable<MapPointPicker> { backStackEntry ->
            val route = backStackEntry.toRoute<MapPointPicker>()
            MapPointPickerScreen(
                field = route.field,
                onBack = { navController.popBackStack() },
                onPointConfirmed = { navController.popBackStack() },
            )
        }
        composable<Notifications> {
            NotificationsScreen(
                onBack = { navController.popBackStack() },
                onOpenDeepLink = { _ -> navController.popBackStack() },
            )
        }
        composable<Chat> { PlaceholderScreen("Chat") }
        composable<Receipt> { PlaceholderScreen("Receipt") }
        composable<Shop> { PlaceholderScreen("Shop") }
    }
}

@Composable
fun PlaceholderScreen(
    name: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = stringResource(Res.string.placeholder_screen, name))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsPlaceholderScreen(
    modifier: Modifier = Modifier,
    languageSwitcher: LanguageSwitcher = koinInject(),
    appConfig: AppConfig = koinInject(),
    onNavigateToDebug: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.settings_language_label),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { languageSwitcher.setLanguage(AppLanguage.ARABIC) },
            ) {
                Text(text = stringResource(Res.string.language_arabic))
            }
            Button(
                onClick = { languageSwitcher.setLanguage(AppLanguage.ENGLISH) },
            ) {
                Text(text = stringResource(Res.string.language_english))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(Res.string.settings_version, appConfig.versionName),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.combinedClickable(
                onLongClick = {
                    if (appConfig.env == Env.DEV) {
                        onNavigateToDebug()
                    }
                },
                onClick = {},
            ),
        )
    }
}
