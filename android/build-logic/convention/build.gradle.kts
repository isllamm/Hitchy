plugins {
    `kotlin-dsl`
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    compileOnly("com.android.tools.build:gradle:${libs.findVersion("agp").get()}")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.findVersion("kotlin").get()}")
    compileOnly("org.jetbrains.kotlin:compose-compiler-gradle-plugin:${libs.findVersion("kotlin").get()}")
    compileOnly("org.jetbrains.kotlin:kotlin-serialization:${libs.findVersion("kotlin").get()}")
    compileOnly("org.jetbrains.compose:compose-gradle-plugin:${libs.findVersion("composeMultiplatform").get()}")
    implementation("com.google.gms:google-services:${libs.findVersion("googleServicesPlugin").get()}")
    implementation("com.google.firebase:firebase-crashlytics-gradle:${libs.findVersion("crashlyticsPlugin").get()}")
}
