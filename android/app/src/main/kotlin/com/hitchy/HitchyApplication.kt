package com.hitchy

import android.app.Application
import com.hitchy.core.data.FirebaseModule
import com.hitchy.core.model.AppConfig
import com.hitchy.core.model.Env

class HitchyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val appConfig = AppConfig(
            env = if (BuildConfig.ENV == "prod") Env.PROD else Env.DEV,
            functionsRegion = BuildConfig.FUNCTIONS_REGION,
            useEmulator = BuildConfig.USE_EMULATOR
        )

        FirebaseModule.init(this, appConfig)
    }
}
