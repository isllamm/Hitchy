package com.hitchy.core.platform

import com.hitchy.core.model.AppLanguage
import kotlin.test.Test
import kotlin.test.assertEquals

class FakeLanguageSwitcherTest {

    @Test
    fun defaultLanguageIsArabic() {
        val switcher = FakeLanguageSwitcher()
        assertEquals(AppLanguage.ARABIC, switcher.getCurrentLanguage())
    }

    @Test
    fun switchLanguageUpdatesCurrentLanguage() {
        val switcher = FakeLanguageSwitcher()
        switcher.setLanguage(AppLanguage.ENGLISH)
        assertEquals(AppLanguage.ENGLISH, switcher.getCurrentLanguage())

        switcher.setLanguage(AppLanguage.ARABIC)
        assertEquals(AppLanguage.ARABIC, switcher.getCurrentLanguage())
    }
}
