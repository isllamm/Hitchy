package com.hitchy

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class KonsistArchitectureTest {

    @Test
    fun `core model and domain packages do not import firebase or android`() {
        Konsist.scopeFromProduction()
            .files
            .filter { it.hasPackage("..domain..") || it.hasPackage("..core.model..") }
            .flatMap { it.imports }
            .assertTrue { import ->
                !import.hasNameStartingWith("dev.gitlive") &&
                    !import.hasNameStartingWith("com.google.firebase") &&
                    !import.hasNameStartingWith("android.") &&
                    !import.hasNameStartingWith("androidx.")
            }
    }

    @Test
    fun `classes ending in ViewModel live in presentation package`() {
        Konsist.scopeFromProduction()
            .classes()
            .withNameEndingWith("ViewModel")
            .filterNot { it.name == "MviViewModel" }
            .assertTrue { it.resideInPackage("..presentation..") }
    }

    @Test
    fun `ui packages never import data packages`() {
        Konsist.scopeFromProduction()
            .files
            .filter { it.hasPackage("..ui..") }
            .flatMap { it.imports }
            .assertTrue { import ->
                !import.hasNameContaining(".data.") ||
                    (import.hasNameEndingWith("core.data.AppError") && import.containingFile.hasPackage("..core.ui.."))
            }
    }

    @Test
    fun `repository implementations live in data package and implement domain repository interface`() {
        Konsist.scopeFromProduction()
            .classes()
            .withNameEndingWith("RepositoryImpl")
            .assertTrue { cls ->
                cls.resideInPackage("..data..") &&
                    cls.parentInterfaces().any { it.name.endsWith("Repository") }
            }
    }
}
