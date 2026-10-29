package com.hitchy.core.platform

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.hitchy.core.model.AppLanguage

class AndroidLanguageSwitcher : LanguageSwitcher {
    override fun getCurrentLanguage(): AppLanguage {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) {
            // TODO(DECISION-04): default language Arabic
            return AppLanguage.ARABIC
        }
        val firstTag = locales[0]?.toLanguageTag() ?: locales.toLanguageTags()
        return if (firstTag.startsWith("en", ignoreCase = true)) {
            AppLanguage.ENGLISH
        } else {
            // TODO(DECISION-04): default language Arabic
            AppLanguage.ARABIC
        }
    }

    override fun setLanguage(language: AppLanguage) {
        val localeList = LocaleListCompat.forLanguageTags(language.code)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}
