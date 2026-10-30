import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("com.android.lint")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

configure<com.android.build.api.dsl.Lint> {
    baseline = file("lint-baseline.xml")
    abortOnError = true
}

configure<KotlinMultiplatformExtension> {
    jvmToolchain(17)

    android {
        namespace = "com.hitchy.${project.name}"
        compileSdk = 37
        minSdk = 26
        withHostTest {}
    }

    sourceSets.apply {
        commonMain.dependencies {
            implementation(libs.findLibrary("compose-material3").get())
            implementation(libs.findLibrary("compose-resources").get())
            implementation(libs.findLibrary("compose-ui-tooling-preview").get())
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
