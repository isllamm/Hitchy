package com.hitchy.core.util

object PhoneNumber {
    private val VALID_E164_REGEX = Regex("""^\+20(1[0125]\d{8})$""")
    private val NATIONAL_REGEX = Regex("""^0(1[0125]\d{8})$""")
    private val INTL_00_REGEX = Regex("""^0020(1[0125]\d{8})$""")
    private val INTL_20_REGEX = Regex("""^20(1[0125]\d{8})$""")

    /**
     * "01012345678", "+201012345678", "00201012345678" → "+201012345678"; null when invalid.
     * Valid: 11 digits starting 010, 011, 012 or 015. Arabic-Indic digits are accepted.
     */
    fun normalize(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        val ascii = buildString(trimmed.length) {
            for (ch in trimmed) {
                when (ch) {
                    in '0'..'9' -> append(ch)
                    in '\u0660'..'\u0669' -> append((ch - '\u0660' + '0'.code).toChar())
                    in '\u06F0'..'\u06F9' -> append((ch - '\u06F0' + '0'.code).toChar())
                    '+' -> if (isEmpty()) append('+') else return null
                    ' ', '-', '.', '(', ')' -> continue
                    else -> return null
                }
            }
        }

        VALID_E164_REGEX.matchEntire(ascii)?.let { return it.value }
        NATIONAL_REGEX.matchEntire(ascii)?.let { return "+20${it.groupValues[1]}" }
        INTL_00_REGEX.matchEntire(ascii)?.let { return "+20${it.groupValues[1]}" }
        INTL_20_REGEX.matchEntire(ascii)?.let { return "+20${it.groupValues[1]}" }

        return null
    }
}
