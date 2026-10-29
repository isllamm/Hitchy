package com.hitchy.core.platform

import com.hitchy.core.model.AppLanguage

interface LanguageSwitcher {
    fun getCurrentLanguage(): AppLanguage
    fun setLanguage(language: AppLanguage)
}
