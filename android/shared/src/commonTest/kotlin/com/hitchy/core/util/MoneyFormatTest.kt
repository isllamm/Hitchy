package com.hitchy.core.util

import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyFormatTest {

    @Test
    fun formatsZero() {
        assertEquals("0", MoneyFormat.formatAmount(0))
    }

    @Test
    fun formatsTypicalRidePrices() {
        assertEquals("45", MoneyFormat.formatAmount(45))
        assertEquals("56", MoneyFormat.formatAmount(56))
        assertEquals("99", MoneyFormat.formatAmount(99))
        assertEquals("179", MoneyFormat.formatAmount(179))
        assertEquals("249", MoneyFormat.formatAmount(249))
    }

    @Test
    fun formatsThousandsWithoutSeparators() {
        assertEquals("1000", MoneyFormat.formatAmount(1000))
        assertEquals("2500", MoneyFormat.formatAmount(2500))
    }

    @Test
    fun formatsNegativeAmounts() {
        assertEquals("-10", MoneyFormat.formatAmount(-10))
    }
}
