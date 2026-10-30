package com.hitchy.core.model

enum class Env {
    DEV,
    PROD,
}

data class AppConfig(
    val env: Env,
    val functionsRegion: String,
    val useEmulator: Boolean,
    val versionName: String,
    /** Where the Firebase emulators run: 10.0.2.2 from an Android emulator, the Mac's address from a real phone. */
    val emulatorHost: String = "10.0.2.2",
)
