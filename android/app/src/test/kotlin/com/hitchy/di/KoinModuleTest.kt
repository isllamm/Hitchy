package com.hitchy.di

import android.app.Application
import android.content.Context
import com.hitchy.core.model.AppConfig
import com.hitchy.core.model.Env
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify
import kotlin.test.Test

@OptIn(KoinExperimentalAPI::class)
class KoinModuleTest {

    @Test
    fun verifyKoinConfiguration() {
        val testAppConfig = AppConfig(
            env = Env.DEV,
            functionsRegion = "europe-west1",
            useEmulator = true,
            versionName = "1.0.0-dev"
        )

        val allModules = module {
            includes(appModule(testAppConfig), coreModule)
            includes(featureModules)
        }

        allModules.verify(
            extraTypes = listOf(
                Context::class,
                Application::class,
                Env::class,
                Boolean::class,
            )
        )
    }
}
