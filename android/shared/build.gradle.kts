plugins {
    id("hitchy.kmp.library")
    id("hitchy.quality")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.gitlive.firebase.auth)
            implementation(libs.gitlive.firebase.firestore)
            implementation(libs.gitlive.firebase.functions)
            implementation(libs.gitlive.firebase.storage)
            implementation(project.dependencies.platform(libs.koin.bom.get()))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.coil.compose)
        }
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.android.bom.get()))
            implementation(libs.firebase.common)
            implementation(libs.firebase.auth)
            implementation(libs.firebase.firestore)
            implementation(libs.firebase.functions)
            implementation(libs.firebase.storage)
            implementation(libs.firebase.appcheck.playintegrity)
            implementation(libs.firebase.appcheck.debug)
            implementation(libs.firebase.crashlytics)
            implementation(libs.firebase.messaging)
            implementation(libs.firebase.config)
            implementation(libs.androidx.appcompat)
            implementation(libs.play.services.location)
            implementation(libs.play.services.maps)
            implementation(libs.maps.compose)
            implementation(libs.coil.network.okhttp)
            implementation(libs.koin.android)
            implementation(libs.androidx.datastore.preferences)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.konsist)
        }
    }
}

val fixturesDir = rootProject.file("../contract/fixtures")
tasks.withType<Test>().configureEach {
    inputs.dir(fixturesDir).withPropertyName("contractFixturesDir")
    systemProperty("hitchy.contract.fixturesDir", fixturesDir.absolutePath)
}

// Compose Multiplatform registers `copyAndroidMainComposeResourcesToAndroidAssets`
// but configures its `outputDirectory` only for `com.android.library`. This module
// uses AGP's newer `com.android.kotlin.multiplatform.library`, which that wiring
// does not reach, so the task is left without an output, never runs, and the APK
// ships with no `composeResources` at all — the app then dies on its first string
// lookup with `MissingResourceException`. Nothing fails before that: the build is
// green, the install succeeds, and the crash only appears on launch.
//
// `matching { }.configureEach { }` rather than `named(...)`, because Compose
// registers the task after this script is evaluated, and reflection for the
// property because the task type is internal to the plugin.
tasks.matching { it.name == "copyAndroidMainComposeResourcesToAndroidAssets" }
    .configureEach {
        val output = this::class.java.methods
            .first { it.name == "getOutputDirectory" }
            .invoke(this) as DirectoryProperty
        output.set(layout.buildDirectory.dir("generated/composeResourcesAssets"))
    }
