package com.hitchy.core.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PhoneNumberTest {

    @Test
    fun normalizesAllValidCarrierPrefixes() {
        assertEquals("+201012345678", PhoneNumber.normalize("01012345678"))
        assertEquals("+201112345678", PhoneNumber.normalize("01112345678"))
        assertEquals("+201212345678", PhoneNumber.normalize("01212345678"))
        assertEquals("+201512345678", PhoneNumber.normalize("01512345678"))
    }

    @Test
    fun normalizesInternationalFormats() {
        assertEquals("+201012345678", PhoneNumber.normalize("+201012345678"))
        assertEquals("+201112345678", PhoneNumber.normalize("00201112345678"))
        assertEquals("+201212345678", PhoneNumber.normalize("201212345678"))
    }

    @Test
    fun normalizesArabicIndicDigits() {
        assertEquals("+201012345678", PhoneNumber.normalize("٠١٠١٢٣٤٥٦٧٨"))
        assertEquals("+201112345678", PhoneNumber.normalize("+٢٠١١١٢٣٤٥٦٧٨"))
        assertEquals("+201512345678", PhoneNumber.normalize("٠٠٢٠١٥١٢٣٤٥٦٧٨"))
    }

    @Test
    fun normalizesEasternArabicIndicDigits() {
        assertEquals("+201012345678", PhoneNumber.normalize("۰۱۰۱۲۳۴۵۶۷۸"))
        assertEquals("+201212345678", PhoneNumber.normalize("+۲۰۱۲۱۲۳۴۵۶۷۸"))
    }

    @Test
    fun ignoresWhitespaceAndFormattingCharacters() {
        assertEquals("+201012345678", PhoneNumber.normalize("010 1234 5678"))
        assertEquals("+201012345678", PhoneNumber.normalize("+20 10 1234 5678"))
        assertEquals("+201112345678", PhoneNumber.normalize("+20-11-1234-5678"))
        assertEquals("+201212345678", PhoneNumber.normalize("(012) 1234-5678"))
        assertEquals("+201512345678", PhoneNumber.normalize("015.1234.5678"))
        assertEquals("+201012345678", PhoneNumber.normalize("  01012345678  \t\n"))
    }

    @Test
    fun rejectsInvalidCarriers() {
        assertNull(PhoneNumber.normalize("01312345678"))
        assertNull(PhoneNumber.normalize("01412345678"))
        assertNull(PhoneNumber.normalize("01612345678"))
        assertNull(PhoneNumber.normalize("01712345678"))
        assertNull(PhoneNumber.normalize("01812345678"))
        assertNull(PhoneNumber.normalize("01912345678"))
        assertNull(PhoneNumber.normalize("+201312345678"))
        assertNull(PhoneNumber.normalize("00201412345678"))
    }

    @Test
    fun rejectsInvalidLengths() {
        assertNull(PhoneNumber.normalize("0101234567")) // 10 digits
        assertNull(PhoneNumber.normalize("010123456789")) // 12 digits
        assertNull(PhoneNumber.normalize("+20101234567")) // short E164
        assertNull(PhoneNumber.normalize("+2010123456789")) // long E164
        assertNull(PhoneNumber.normalize("0020101234567")) // short 0020
    }

    @Test
    fun rejectsNonEgyptianCountryCodes() {
        assertNull(PhoneNumber.normalize("+11012345678"))
        assertNull(PhoneNumber.normalize("+441012345678"))
        assertNull(PhoneNumber.normalize("+9661012345678"))
    }

    @Test
    fun rejectsInvalidCharactersAndMalformedPlus() {
        assertNull(PhoneNumber.normalize("0101234abcd"))
        assertNull(PhoneNumber.normalize("+2010abc5678"))
        assertNull(PhoneNumber.normalize("010+12345678"))
        assertNull(PhoneNumber.normalize("++201012345678"))
        assertNull(PhoneNumber.normalize("010#12345678"))
        assertNull(PhoneNumber.normalize("010@12345678"))
    }

    @Test
    fun rejectsEmptyAndBlankInputs() {
        assertNull(PhoneNumber.normalize(""))
        assertNull(PhoneNumber.normalize("   "))
        assertNull(PhoneNumber.normalize("+20"))
        assertNull(PhoneNumber.normalize("0020"))
    }
}
