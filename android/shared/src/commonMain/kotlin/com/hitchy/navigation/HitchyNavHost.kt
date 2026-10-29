package com.hitchy.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hitchy.core.model.AppConfig
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.Env
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.feature.debug.ui.DebugScreen
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.language_arabic
import hitchy.shared.generated.resources.language_english
import hitchy.shared.generated.resources.placeholder_screen
import hitchy.shared.generated.resources.settings_language_label
import hitchy.shared.generated.resources.settings_title
import hitchy.shared.generated.resources.settings_version
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun HitchyNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Any = Welcome,
    appConfig: AppConfig = koinInject(),
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<Welcome> { PlaceholderScreen("Welcome") }
        composable<PhoneEntry> { PlaceholderScreen("PhoneEntry") }
        composable<OtpVerify> { PlaceholderScreen("OtpVerify") }
        composable<ProfileSetup> { PlaceholderScreen("ProfileSetup") }
        composable<Home> { PlaceholderScreen("Home") }
        composable<Settings> {
            SettingsPlaceholderScreen(
                onNavigateToDebug = { navController.navigate(Debug) },
            )
        }
        composable<HostApplication> { PlaceholderScreen("HostApplication") }
        composable<HostStatus> { PlaceholderScreen("HostStatus") }
        composable<AddVehicle> { PlaceholderScreen("AddVehicle") }
        composable<PostRide> { PlaceholderScreen("PostRide") }
        composable<MyRides> { PlaceholderScreen("MyRides") }
        composable<HostRideDetails> { PlaceholderScreen("HostRideDetails") }
        composable<Search> { PlaceholderScreen("Search") }
        composable<SearchResults> { PlaceholderScreen("SearchResults") }
        composable<RiderRideDetails> { PlaceholderScreen("RiderRideDetails") }
        composable<MyTrips> { PlaceholderScreen("MyTrips") }
        composable<HostTrip> { PlaceholderScreen("HostTrip") }
        composable<RiderTrip> { PlaceholderScreen("RiderTrip") }
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
                )
            }
        }
        composable<Onboarding> { PlaceholderScreen("Onboarding") }
        composable<RoutePicker> { PlaceholderScreen("RoutePicker") }
        composable<MapPointPicker> { PlaceholderScreen("MapPointPicker") }
        composable<Notifications> { PlaceholderScreen("Notifications") }
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
