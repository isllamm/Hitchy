package com.hitchy.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.hitchy.core.model.AppConfig
import com.hitchy.core.platform.ActivityProvider
import com.hitchy.core.platform.Analytics
import com.hitchy.core.platform.AndroidActivityProvider
import com.hitchy.core.platform.AndroidDialer
import com.hitchy.core.platform.AndroidImageCompressor
import com.hitchy.core.platform.AndroidLanguageSwitcher
import com.hitchy.core.platform.AndroidLocationProvider
import com.hitchy.core.platform.AndroidSharer
import com.hitchy.core.platform.AppSettings
import com.hitchy.core.platform.DataStoreAppSettings
import com.hitchy.core.platform.Dialer
import com.hitchy.core.platform.ImageCompressor
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.platform.LocationProvider
import com.hitchy.core.platform.NoOpAnalytics
import com.hitchy.core.platform.Sharer
import com.hitchy.feature.debug.data.AndroidDebugInfo
import com.hitchy.feature.debug.domain.DebugInfo
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

val Context.hitchyDataStore: DataStore<Preferences> by preferencesDataStore(name = "hitchy_settings")

fun appModule(appConfig: AppConfig): Module = module {
    single<AppConfig> { appConfig }
    single<DataStore<Preferences>> { androidContext().hitchyDataStore }
    single<AppSettings> { DataStoreAppSettings(get()) }
    single<LanguageSwitcher> { AndroidLanguageSwitcher() }

    single { AndroidActivityProvider() }
    single<ActivityProvider> { get<AndroidActivityProvider>() }

    single<LocationProvider> { AndroidLocationProvider(androidContext()) }
    single<ImageCompressor> { AndroidImageCompressor() }
    single<Sharer> { AndroidSharer(androidContext(), getOrNull()) }
    single<Dialer> { AndroidDialer(androidContext(), getOrNull()) }

    // TODO(P14-A01): Firebase Analytics and real event logging
    single<Analytics> { NoOpAnalytics() }

    single<DebugInfo> { AndroidDebugInfo(get()) }

    // TODO(P4-A01): PhoneAuth binding
    // TODO(P8-A02): TripTracker binding
}
