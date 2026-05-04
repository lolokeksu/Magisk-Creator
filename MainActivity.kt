package com.magisk.next

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.magisk.next.ui.LocaleHelper
import com.magisk.next.ui.MainScreen
import com.magisk.next.ui.theme.MagiskModuleBuilderTheme
import com.magisk.next.viewmodel.ModuleViewModel

class MainActivity : ComponentActivity() {

    private lateinit var appSettings: AppSettings
    private val moduleViewModel: ModuleViewModel by viewModels()

    override fun attachBaseContext(newBase: Context?) {
        val context = newBase ?: baseContext
        val savedLang = LocaleHelper.getSavedLanguage(context)
        val wrappedContext = LocaleHelper.applySavedLocale(context)
        super.attachBaseContext(wrappedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appSettings = AppSettings(this)
        Logger.setLevel(appSettings.logLevel)

        setContent {
            MagiskModuleBuilderTheme {
                MainScreen(viewModel = moduleViewModel)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (appSettings.autoSaveOnExit) {
            try {
                val autosaveFile = java.io.File(cacheDir, "autosave.mmproj")
                val uri =
                    androidx.core.content.FileProvider.getUriForFile(this, "${packageName}.fileprovider", autosaveFile)
                moduleViewModel.saveProject(this, uri)
            } catch (e: Exception) {
                Logger.logError("Autosave failed: ${e.message}")
            }
        }
    }
}