package com.hitchy.core.platform

import com.hitchy.core.model.AppLanguage

class FakeLanguageSwitcher(
    // TODO(DECISION-04): default language Arabic
    initialLanguage: AppLanguage = AppLanguage.ARABIC,
) : LanguageSwitcher {
    private var current: AppLanguage = initialLanguage

    override fun getCurrentLanguage(): AppLanguage = current

    override fun setLanguage(language: AppLanguage) {
        current = language
    }
}
