package com.hitchy.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.hitchy.core.model.AppConfig
import com.hitchy.core.platform.AppSettings
import com.hitchy.core.platform.DataStoreAppSettings
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

val Context.hitchyDataStore: DataStore<Preferences> by preferencesDataStore(name = "hitchy_settings")

fun appModule(appConfig: AppConfig): Module = module {
    single<AppConfig> { appConfig }
    single<DataStore<Preferences>> { androidContext().hitchyDataStore }
    single<AppSettings> { DataStoreAppSettings(get()) }
}
