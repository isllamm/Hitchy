package com.hitchy.core.platform

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.hitchy.core.model.AppLanguage
import java.util.Locale

class AndroidLanguageSwitcher : LanguageSwitcher {
    // The strings follow the locale the system gives the app, so names from the data follow it too.
    override fun getCurrentLanguage(): AppLanguage {
        val tag = Locale.getDefault().language
        return if (tag.equals("ar", ignoreCase = true)) AppLanguage.ARABIC else AppLanguage.ENGLISH
    }

    override fun setLanguage(language: AppLanguage) {
        val localeList = LocaleListCompat.forLanguageTags(language.code)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}
