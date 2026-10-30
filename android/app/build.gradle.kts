plugins {
    id("hitchy.android.application")
    id("hitchy.quality")
}

// `:shared` generates its Compose resources but cannot package them itself — see
// the note in `shared/build.gradle.kts`. They are added here as an assets
// directory so they reach the APK, with the generating task as the dependency so
// asset merging never runs before they exist.
android {
    sourceSets.named("main") {
        // A plain path, not a Provider: AGP rejects Providers in the source-set
        // API because it cannot tell generated directories from hand-written ones.
        // The task dependency below is what guarantees it exists in time.
        assets.srcDir(
            project(":shared").layout.buildDirectory
                .dir("generated/composeResourcesAssets").get().asFile
        )
    }
}

tasks.matching { it.name.matches(Regex("merge.*Assets")) }.configureEach {
    dependsOn(":shared:copyAndroidMainComposeResourcesToAndroidAssets")
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.material3)

    implementation(platform(libs.firebase.android.bom))
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

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.koin.test)

    configurations.matching { it.name == "devDebugImplementation" }.all {
        dependencies.add(project.dependencies.create(libs.leakcanary.android.get()))
    }
}
