package com.hitchy.di

import com.hitchy.core.data.FunctionsClient
import com.hitchy.core.data.GitLiveFunctionsClient
import com.hitchy.core.model.AppConfig
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.functions.functions
import kotlin.time.Clock
import org.koin.dsl.module

val coreModule = module {
    single<Clock> { Clock.System }
    single<FunctionsClient> {
        val config = get<AppConfig>()
        GitLiveFunctionsClient(Firebase.functions(region = config.functionsRegion))
    }
}
