package com.hitchy.di

import org.koin.dsl.module

val authModule = module { }
val profileModule = module { }
val hostModule = module { }
val ridesModule = module { }
val bookingModule = module { }
val tripModule = module { }
val packagesModule = module { }
val rewardsModule = module { }
val assistantModule = module { }
val fixedModule = module { }
val safetyModule = module { }
val debugModule = module { }
val placesModule = module { }
val chatModule = module { }
val inboxModule = module { }

val featureModules = listOf(
    authModule,
    profileModule,
    hostModule,
    ridesModule,
    bookingModule,
    tripModule,
    packagesModule,
    rewardsModule,
    assistantModule,
    fixedModule,
    safetyModule,
    debugModule,
    placesModule,
    chatModule,
    inboxModule
)
