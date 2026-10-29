plugins {
    id("hitchy.android.application")
    id("hitchy.quality")
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
