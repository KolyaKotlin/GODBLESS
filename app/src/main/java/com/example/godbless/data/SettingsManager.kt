package com.example.godbless.data

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME = "theme"
        private const val KEY_LANGUAGE = "language"

        const val THEME_LIGHT = "Светлая"
        const val THEME_DARK = "Темная"
        const val THEME_SYSTEM = "Системная"

        const val LANG_RUSSIAN = "Русский"
        const val LANG_ENGLISH = "English"
        const val LANG_CHINESE = "中文" // Китайский
    }

    fun getTheme(): String {
        return prefs.getString(KEY_THEME, THEME_SYSTEM) ?: THEME_SYSTEM
    }

    fun setTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
    }

    fun getLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, LANG_RUSSIAN) ?: LANG_RUSSIAN
    }

    fun setLanguage(language: String) {
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
    }
}
