package com.example.godbless.utils

import android.content.Context
import android.content.res.Configuration
import com.example.godbless.data.SettingsManager
import java.util.*

object LocaleHelper {

    fun setLocale(context: Context, languageCode: String): Context {
        val locale = getLocaleFromLanguage(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    fun getLocaleFromLanguage(languageName: String): Locale {
        return when (languageName) {
            SettingsManager.LANG_RUSSIAN -> Locale("ru")
            SettingsManager.LANG_ENGLISH -> Locale("en")
            SettingsManager.LANG_CHINESE -> Locale("zh")
            else -> Locale("ru") // По умолчанию русский
        }
    }

    fun applyLanguage(context: Context, languageName: String) {
        val locale = getLocaleFromLanguage(languageName)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)

        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}
