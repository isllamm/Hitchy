package com.hitchy.core.model

enum class Env {
    DEV,
    PROD,
}

data class AppConfig(
    val env: Env,
    val functionsRegion: String,
    val useEmulator: Boolean,
)
