import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

val prodGoogleServices = file("src/prod/google-services.json")
val isProdPlaceholder =
    if (!prodGoogleServices.exists() || prodGoogleServices.length() < 10) {
        prodGoogleServices.parentFile.mkdirs()
        prodGoogleServices.writeText(
            """
            {
              "project_info": {
                "project_number": "000000000000",
                "project_id": "demo-hitchy",
                "storage_bucket": "demo-hitchy.appspot.com"
              },
              "client": [
                {
                  "client_info": {
                    "mobilesdk_app_id": "1:000000000000:android:0000000000000000000000",
                    "android_client_info": {
                      "package_name": "com.hitchy.app"
                    }
                  },
                  "oauth_client": [],
                  "api_key": [
                    {
                      "current_key": "AIzaSyDummyPlaceholderKeyForLocalBuildsOnly"
                    }
                  ],
                  "services": {}
                }
              ],
              "configuration_version": "1"
            }
            """.trimIndent(),
        )
        logger.warn("WARNING: app/src/prod/google-services.json missing; generated demo-hitchy placeholder.")
        true
    } else {
        prodGoogleServices.readText().contains("demo-hitchy")
    }

val devGoogleServices = file("src/dev/google-services.json")
if (!devGoogleServices.exists() || devGoogleServices.length() < 10) {
    devGoogleServices.parentFile.mkdirs()
    devGoogleServices.writeText(
        """
        {
          "project_info": {
            "project_number": "000000000000",
            "project_id": "demo-hitchy",
            "storage_bucket": "demo-hitchy.appspot.com"
          },
          "client": [
            {
              "client_info": {
                "mobilesdk_app_id": "1:000000000000:android:0000000000000000000000",
                "android_client_info": {
                  "package_name": "com.hitchy.app.dev"
                }
              },
              "oauth_client": [],
              "api_key": [
                {
                  "current_key": "AIzaSyDummyPlaceholderKeyForLocalBuildsOnly"
                }
              ],
              "services": {}
            }
          ],
          "configuration_version": "1"
        }
        """.trimIndent(),
    )
    logger.warn("WARNING: app/src/dev/google-services.json missing; generated demo-hitchy placeholder.")
}

val versionCodeProp = providers.gradleProperty("hitchy.versionCode").orNull?.toIntOrNull() ?: 1
val versionNameProp = providers.gradleProperty("hitchy.versionName").orNull ?: "0.1.0"

// BUILD_PLAN 5.10: the Maps key comes from the MAPS_API_KEY environment variable (CI) or from
// android/local.properties (git-ignored). A missing key is an empty value: the build still passes.
val mapsApiKey: String =
    providers
        .environmentVariable("MAPS_API_KEY")
        .orElse(
            providers
                .fileContents(rootProject.layout.projectDirectory.file("local.properties"))
                .asText
                .map { text -> Properties().apply { load(text.reader()) }.getProperty("MAPS_API_KEY", "") },
        ).map { it.trim() }
        .getOrElse("")

configure<ApplicationExtension> {
    namespace = "com.hitchy"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.hitchy.app"
        minSdk = 26
        targetSdk = 36
        versionCode = versionCodeProp
        versionName = versionNameProp

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "FUNCTIONS_REGION", "\"europe-west1\"")
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    val keystorePath = System.getenv("HITCHY_KEYSTORE_PATH")
    val keystorePassword = System.getenv("HITCHY_KEYSTORE_PASSWORD")
    val keyAlias = System.getenv("HITCHY_KEY_ALIAS")
    val keyPassword = System.getenv("HITCHY_KEY_PASSWORD")
    val isCi = System.getenv("CI")?.toBoolean() == true

    val hasKeystoreVars =
        !keystorePath.isNullOrBlank() &&
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
    } else if (!isCi) {
        logger.warn("WARNING: Release keystore environment variables unset; falling back to debug signing key for local build.")
    }

    if (isCi) {
        gradle.taskGraph.whenReady {
            val isReleaseBuild = allTasks.any { it.name.contains("Release", ignoreCase = true) }
            if (isReleaseBuild && !hasKeystoreVars) {
                throw org.gradle.api.GradleException(
                    "Missing release keystore environment variables (HITCHY_KEYSTORE_PATH, HITCHY_KEYSTORE_PASSWORD, HITCHY_KEY_ALIAS, HITCHY_KEY_PASSWORD) in CI environment.",
                )
            }
            val isProdRelease = allTasks.any { it.name.contains("ProdRelease", ignoreCase = true) }
            if (isProdRelease && isProdPlaceholder) {
                throw org.gradle.api.GradleException(
                    "Cannot build prodRelease with placeholder google-services.json in CI. Please provide GOOGLE_SERVICES_PROD secret.",
                )
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig =
                if (hasKeystoreVars) {
                    signingConfigs.getByName("release")
                } else {
                    signingConfigs.getByName("debug")
                }
        }
        debug {
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

    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = true
    }
}

extensions.findByType(KotlinBaseExtension::class.java)?.jvmToolchain(17)
