package com.hitchy.navigation

import com.hitchy.core.model.Role

sealed interface DeepLinkTarget {
    data class Screen(val route: Any) : DeepLinkTarget
    data class Browser(val url: String) : DeepLinkTarget
}

object DeepLinks {
    fun parse(uri: String, role: Role): DeepLinkTarget? {
        val trimmed = uri.trim()
        if (trimmed.startsWith("https://", ignoreCase = true)) {
            val afterScheme = trimmed.substring("https://".length)
            val hostPart = afterScheme.takeWhile { it != '/' && it != '?' && it != '#' && it != ':' }
            if (hostPart.isEmpty() || hostPart.contains(' ')) {
                return null
            }
            return DeepLinkTarget.Browser(trimmed)
        }

        if (!trimmed.startsWith("hitchy://", ignoreCase = true)) {
            return null
        }

        val path = trimmed.substring("hitchy://".length)
        if (path.contains('?') || path.contains('#')) {
            return null
        }

        val segments = path.split('/')
        if (segments.isEmpty() || segments.any { it.isEmpty() }) {
            return null
        }

        return when (segments.size) {
            1 -> when (segments[0]) {
                "home" -> DeepLinkTarget.Screen(Home)
                "notifications" -> DeepLinkTarget.Screen(Notifications)
                "search" -> DeepLinkTarget.Screen(Search)
                "trips" -> DeepLinkTarget.Screen(MyTrips)
                "fees" -> DeepLinkTarget.Screen(Fees)
                "packages" -> DeepLinkTarget.Screen(Packages)
                "rewards" -> DeepLinkTarget.Screen(Rewards)
                "shop" -> DeepLinkTarget.Screen(Shop)
                "profile" -> {
                    // TODO(P4-A04): open the Profile tab (Profile is a bottom tab in v1.1, plan 2.1 #9; not Settings, not ProfileSetup)
                    DeepLinkTarget.Screen(Home)
                }
                else -> null
            }
            2 -> when (segments[0]) {
                "ride" -> {
                    val id = segments[1]
                    val route = if (role == Role.HOST) HostRideDetails(id) else RiderRideDetails(id)
                    DeepLinkTarget.Screen(route)
                }
                "trip" -> {
                    val id = segments[1]
                    val route = if (role == Role.HOST) HostTrip(id) else RiderTrip(id)
                    DeepLinkTarget.Screen(route)
                }
                "rate" -> DeepLinkTarget.Screen(RateTrip(segments[1]))
                "fixed" -> DeepLinkTarget.Screen(FixedRideDetails(segments[1]))
                "backup" -> DeepLinkTarget.Screen(BackupOptions(cancelledRideId = segments[1]))
                "receipt" -> DeepLinkTarget.Screen(Receipt(segments[1]))
                "host" -> if (segments[1] == "status") DeepLinkTarget.Screen(HostStatus) else null
                else -> null
            }
            3 -> when (segments[0]) {
                "chat" -> DeepLinkTarget.Screen(Chat(rideId = segments[1], riderUid = segments[2]))
                else -> null
            }
            else -> null
        }
    }
}
