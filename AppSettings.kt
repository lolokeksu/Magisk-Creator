package com.magisk.next

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class AppSettings(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    // Существующие
    var savePath: String
        get() = prefs.getString("save_path", "MagiskModuleCreator/Project") ?: "MagiskModuleCreator/Project"
        set(value) = prefs.edit { putString("save_path", value) }

    var hiddenExtensions: Set<String>
        get() = prefs.getStringSet("hidden_extensions", emptySet()) ?: emptySet()
        set(value) = prefs.edit { putStringSet("hidden_extensions", value) }

    var notifyAfterExport: Boolean
        get() = prefs.getBoolean("notify_after_export", true)
        set(value) = prefs.edit { putBoolean("notify_after_export", value) }

    var autoSaveOnExit: Boolean
        get() = prefs.getBoolean("auto_save_on_exit", false)
        set(value) = prefs.edit { putBoolean("auto_save_on_exit", value) }

    var showHiddenFiles: Boolean
        get() = prefs.getBoolean("show_hidden_files", false)
        set(value) = prefs.edit { putBoolean("show_hidden_files", value) }

    var logLevel: String
        get() = prefs.getString("log_level", "errors") ?: "errors"
        set(value) = prefs.edit { putString("log_level", value) }

    // НОВОЕ: Тема оформления
    var themeMode: String
        get() = prefs.getString("theme_mode", "system") ?: "system"
        set(value) = prefs.edit { putString("theme_mode", value) }

    fun exportSettings(): String {
        val map = prefs.all.toMap()
        // Исключаем ключи, которые не должны экспортироваться (если нужно)
        return org.json.JSONObject(map).toString()
    }

    fun importSettings(jsonString: String) {
        try {
            val json = org.json.JSONObject(jsonString)
            val editor = prefs.edit()
            for (key in json.keys()) {
                when (val value = json.get(key)) {
                    is String -> editor.putString(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                }
            }
            editor.apply()
        } catch (_: Exception) {}
    }

    fun clearLogs() {
        val logFile = java.io.File(android.os.Environment.getExternalStorageDirectory(), Logger.LOG_FILE)
        if (logFile.exists()) logFile.delete()
    }
}