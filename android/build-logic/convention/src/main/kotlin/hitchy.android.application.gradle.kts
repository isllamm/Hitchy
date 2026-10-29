import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

val versionCodeProp = providers.gradleProperty("hitchy.versionCode").orNull?.toIntOrNull() ?: 1
val versionNameProp = providers.gradleProperty("hitchy.versionName").orNull ?: "0.1.0"

configure<ApplicationExtension> {
    namespace = "com.hitchy"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.hitchy"
        minSdk = 26
        targetSdk = 36
        versionCode = versionCodeProp
        versionName = versionNameProp

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "FUNCTIONS_REGION", "\"europe-west1\"")
    }

    val keystorePath = System.getenv("HITCHY_KEYSTORE_PATH")
    val keystorePassword = System.getenv("HITCHY_KEYSTORE_PASSWORD")
    val keyAlias = System.getenv("HITCHY_KEY_ALIAS")
    val keyPassword = System.getenv("HITCHY_KEY_PASSWORD")
    val isCi = System.getenv("CI")?.toBoolean() == true

    val hasKeystoreVars = !keystorePath.isNullOrBlank() &&
        !keystorePassword.isNullOrBlank() &&
        !keyAlias.isNullOrBlank() &&
        !keyPassword.isNullOrBlank()

    if (hasKeystoreVars) {
        signingConfigs.create("release") {
            storeFile = file(keystorePath)
            storePassword = keystorePassword
            this.keyAlias = keyAlias
            this.keyPassword = keyPassword
        }
    } else if (isCi) {
        gradle.taskGraph.whenReady {
            val isReleaseBuild = allTasks.any { it.name.contains("Release", ignoreCase = true) }
            if (isReleaseBuild) {
                throw org.gradle.api.GradleException(
                    "Missing release keystore environment variables (HITCHY_KEYSTORE_PATH, HITCHY_KEYSTORE_PASSWORD, HITCHY_KEY_ALIAS, HITCHY_KEY_PASSWORD) in CI environment."
                )
            }
        }
    } else {
        logger.warn("WARNING: Release keystore environment variables unset; falling back to debug signing key for local build.")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (hasKeystoreVars) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    flavorDimensions += "env"
    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev"
            buildConfigField("String", "ENV", "\"dev\"")
            val useEmulator = providers.gradleProperty("hitchy.emulator").orNull == "true"
            buildConfigField("boolean", "USE_EMULATOR", useEmulator.toString())
        }
        create("prod") {
            dimension = "env"
            buildConfigField("String", "ENV", "\"prod\"")
            buildConfigField("boolean", "USE_EMULATOR", "false")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

extensions.findByType(KotlinBaseExtension::class.java)?.jvmToolchain(17)
