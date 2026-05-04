package com.magisk.next

import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object Logger {
    const val LOG_FILE = "MagiskModuleCreator/module_creator.log"

    private var currentLevel: String = "all" // "errors", "errors+warnings", "all"

    fun setLevel(level: String) {
        currentLevel = level
    }

    fun log(message: String, level: String = "info") {
        val shouldLog = when (currentLevel) {
            "errors" -> level == "error"
            "errors+warnings" -> level == "error" || level == "warning"
            "all" -> true
            else -> true
        }
        if (!shouldLog) return

        try {
            val dir = File(Environment.getExternalStorageDirectory(), "MagiskModuleCreator")
            if (!dir.exists()) dir.mkdirs()
            val logFile = File(Environment.getExternalStorageDirectory(), LOG_FILE)
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            logFile.appendText("[$timestamp] [$level] $message\n")
        } catch (_: Exception) {}
    }

    fun logError(message: String) = log(message, "error")
    fun logWarning(message: String) = log(message, "warning")
    fun logInfo(message: String) = log(message, "info")
}