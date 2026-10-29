plugins {
    id("hitchy.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.gitlive.firebase.auth)
            implementation(libs.gitlive.firebase.firestore)
            implementation(libs.gitlive.firebase.functions)
            implementation(libs.gitlive.firebase.storage)
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
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
