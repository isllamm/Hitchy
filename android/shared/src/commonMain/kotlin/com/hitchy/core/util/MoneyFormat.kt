package com.hitchy.core.util

object MoneyFormat {
    /**
     * Formats integer EGP amount as digits only (Western digits, no thousands separator).
     * Currency labels ("EGP %1$s", "%1$s جنيه") live in Compose string resources.
     */
    fun formatAmount(amount: Int): String = amount.toString()
}
