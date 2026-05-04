package com.magisk.next.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.core.os.ConfigurationCompat
import java.util.Locale

object LocaleHelper {
    private const val PREFS_NAME = "app_settings"
    private const val KEY_LANGUAGE = "language"

    fun setLocale(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()

        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }

    fun applySavedLocale(context: Context): Context {
        val languageCode = getSavedLanguage(context)
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}