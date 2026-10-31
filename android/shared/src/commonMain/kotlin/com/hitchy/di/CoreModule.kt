package com.hitchy.di

import com.hitchy.core.data.FunctionsClient
import com.hitchy.core.data.GitLiveFunctionsClient
import com.hitchy.core.data.StoragePhotoUrlResolver
import com.hitchy.core.model.AppConfig
import com.hitchy.core.platform.PhotoUrlResolver
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.functions.functions
import org.koin.dsl.module
import kotlin.time.Clock

val coreModule = module {
    single<Clock> { Clock.System }
    single<PhotoUrlResolver> { StoragePhotoUrlResolver() }
    single<FunctionsClient> {
        val config = get<AppConfig>()
        GitLiveFunctionsClient(Firebase.functions(region = config.functionsRegion))
    }
}
