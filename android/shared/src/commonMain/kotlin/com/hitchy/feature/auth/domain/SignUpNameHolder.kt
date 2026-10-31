package com.hitchy.feature.auth.domain

/**
 * The full name typed on the Sign up screen (Figma 828:15816), kept until Complete profile fills its first
 * and last name from it. Memory only: it is never sent or stored.
 */
class SignUpNameHolder {
    var fullName: String = ""

    /** "Ahmed Mohamed Hassan" → ("Ahmed", "Mohamed Hassan"). */
    fun split(): Pair<String, String> {
        val parts = fullName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return (parts.firstOrNull() ?: "") to parts.drop(1).joinToString(" ")
    }
}
