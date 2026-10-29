package com.hitchy

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.hitchy.core.data.FirebaseModule
import com.hitchy.core.model.AppConfig
import com.hitchy.core.model.Env
import com.hitchy.core.platform.AndroidActivityProvider
import com.hitchy.di.appModule
import com.hitchy.di.coreModule
import com.hitchy.di.featureModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class HitchyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // TODO(DECISION-04): default language Arabic
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ar"))
        }

        val appConfig = AppConfig(
            env = if (BuildConfig.ENV == "prod") Env.PROD else Env.DEV,
            functionsRegion = BuildConfig.FUNCTIONS_REGION,
            useEmulator = BuildConfig.USE_EMULATOR,
            versionName = BuildConfig.VERSION_NAME,
        )

        FirebaseModule.init(this, appConfig)

        val koinApp = startKoin {
            androidLogger(if (appConfig.env == Env.DEV) Level.DEBUG else Level.ERROR)
            androidContext(this@HitchyApplication)
            modules(
                listOf(
                    appModule(appConfig),
                    coreModule,
                ) + featureModules,
            )
        }

        registerActivityLifecycleCallbacks(koinApp.koin.get<AndroidActivityProvider>())
    }
}
