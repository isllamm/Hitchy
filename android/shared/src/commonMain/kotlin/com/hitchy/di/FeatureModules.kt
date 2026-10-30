package com.hitchy.di

import com.hitchy.feature.auth.data.AuthRepositoryImpl
import com.hitchy.feature.auth.domain.AuthRepository
import com.hitchy.feature.auth.presentation.RootViewModel
import com.hitchy.feature.booking.data.BookingRepositoryImpl
import com.hitchy.feature.booking.domain.BookingRepository
import com.hitchy.feature.debug.data.DebugRepositoryImpl
import com.hitchy.feature.debug.domain.DebugRepository
import com.hitchy.feature.debug.presentation.DebugViewModel
import com.hitchy.feature.host.data.HostRepositoryImpl
import com.hitchy.feature.host.domain.HostRepository
import com.hitchy.feature.inbox.data.InboxRepositoryImpl
import com.hitchy.feature.inbox.domain.InboxRepository
import com.hitchy.feature.places.data.PlacesRepositoryImpl
import com.hitchy.feature.places.domain.PlacesRepository
import com.hitchy.feature.profile.data.ProfileRepositoryImpl
import com.hitchy.feature.profile.data.WalletRepositoryImpl
import com.hitchy.feature.profile.domain.ProfileRepository
import com.hitchy.feature.profile.domain.WalletRepository
import com.hitchy.feature.rides.data.RidesRepositoryImpl
import com.hitchy.feature.rides.domain.RidesRepository
import com.hitchy.feature.trip.data.TripRepositoryImpl
import com.hitchy.feature.trip.domain.TripRepository
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single<AuthRepository> { AuthRepositoryImpl() }
    viewModel { RootViewModel(get(), get()) }
}

val profileModule = module {
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    single<WalletRepository> { WalletRepositoryImpl() }
}

val hostModule = module {
    single<HostRepository> { HostRepositoryImpl(get()) }
}

val ridesModule = module {
    single<RidesRepository> { RidesRepositoryImpl(get()) }
}

val bookingModule = module {
    single<BookingRepository> { BookingRepositoryImpl(get()) }
}

val tripModule = module {
    single<TripRepository> { TripRepositoryImpl(get()) }
}

val packagesModule = module { }
val rewardsModule = module { }
val assistantModule = module { }
val fixedModule = module { }
val safetyModule = module { }

val debugModule = module {
    single<DebugRepository> { DebugRepositoryImpl() }
    viewModel { DebugViewModel(get(), get(), get()) }
}

val placesModule = module {
    single<PlacesRepository> { PlacesRepositoryImpl() }
}

val chatModule = module { }

val inboxModule = module {
    single<InboxRepository> { InboxRepositoryImpl() }
}

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
    inboxModule,
)
