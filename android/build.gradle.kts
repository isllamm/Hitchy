plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.spotless)
}

spotless {
    kotlinGradle {
        target("*.gradle.kts", "build-logic/**/*.gradle.kts")
        targetExclude("**/build/**")
        ktlint(libs.versions.ktlint.get())
    }
    kotlin {
        target("build-logic/**/*.kt")
        targetExclude("**/build/**")
        ktlint(libs.versions.ktlint.get())
    }
}

tasks.named("check") {
    dependsOn(tasks.named("spotlessCheck"))
}
