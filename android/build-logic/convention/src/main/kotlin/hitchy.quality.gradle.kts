import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

plugins {
    id("io.gitlab.arturbosch.detekt")
    id("com.diffplug.spotless")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("${rootProject.projectDir}/config/detekt/detekt.yml"))
}

configurations.matching { it.name == "detekt" }.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion("2.0.21")
        }
    }
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    if (project.file("src/commonMain/kotlin").exists()) {
        setSource(
            files(
                "src/commonMain/kotlin",
                "src/androidMain/kotlin",
                "src/commonTest/kotlin",
                "src/androidHostTest/kotlin",
            ),
        )
    }
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint(libs.findVersion("ktlint").get().toString())
            .editorConfigOverride(
                mapOf(
                    "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                    "ktlint_standard_filename" to "disabled",
                ),
            )
    }
}
