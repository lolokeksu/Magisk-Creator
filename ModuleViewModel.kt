package com.magisk.next.viewmodel

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.magisk.next.Logger
import com.magisk.next.model.ModuleFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ModuleViewModel : ViewModel() {

    // Центральное хранилище всех состояний
    internal val data = ModuleData()

    // Делегаты для совместимости с UI
    var moduleId: String
        get() = data.moduleId
        set(value) { data.moduleId = value }
    var moduleName: String
        get() = data.moduleName
        set(value) { data.moduleName = value }
    var moduleVersion: String
        get() = data.moduleVersion
        set(value) { data.moduleVersion = value }
    var moduleVersionCode: String
        get() = data.moduleVersionCode
        set(value) { data.moduleVersionCode = value }
    var moduleAuthor: String
        get() = data.moduleAuthor
        set(value) { data.moduleAuthor = value }
    var moduleLink: String
        get() = data.moduleLink
        set(value) { data.moduleLink = value }
    var moduleDescription: String
        get() = data.moduleDescription
        set(value) { data.moduleDescription = value }
    var moduleChangelog: String
        get() = data.moduleChangelog
        set(value) { data.moduleChangelog = value }
    var customizeScript: String
        get() = data.customizeScript
        set(value) { data.customizeScript = value }
    var serviceScript: String
        get() = data.serviceScript
        set(value) { data.serviceScript = value }
    var postFsScript: String
        get() = data.postFsScript
        set(value) { data.postFsScript = value }
    var updateBinaryType: String
        get() = data.updateBinaryType
        set(value) { data.updateBinaryType = value }
    var updateBinaryCustom: String
        get() = data.updateBinaryCustom
        set(value) { data.updateBinaryCustom = value }
    var minMagisk: String
        get() = data.minMagisk
        set(value) { data.minMagisk = value }
    var systemless: Boolean
        get() = data.systemless
        set(value) { data.systemless = value }
    var needsystem: Boolean
        get() = data.needsystem
        set(value) { data.needsystem = value }
    var skipMount: Boolean
        get() = data.skipMount
        set(value) { data.skipMount = value }
    var recoveryMode: Boolean
        get() = data.recoveryMode
        set(value) { data.recoveryMode = value }
    var replaceFiles: String
        get() = data.replaceFiles
        set(value) { data.replaceFiles = value }
    var sepolicyRules: String
        get() = data.sepolicyRules
        set(value) { data.sepolicyRules = value }
    var verifyKey: String
        get() = data.verifyKey
        set(value) { data.verifyKey = value }

    val moduleFiles = data.moduleFiles

    // Вспомогательные компоненты
    private val validator get() = ModuleValidator(
        moduleId, moduleName, moduleVersion, moduleDescription,
        customizeScript, moduleFiles.size
    )
    private val builder = ModuleBuilder(data)
    private val projectManager = ProjectManager(data)
    private val importer = ModuleImporter(data)
    private val templateEngine = TemplateEngine(data)

    // Результаты валидации
    private val _validationResults = MutableStateFlow<List<ValidationItem>>(emptyList())
    val validationResults: StateFlow<List<ValidationItem>> = _validationResults

    // ---------- Публичные методы ----------
    fun validate(context: Context): List<ValidationItem> {
        val items = validator.validate(context)
        _validationResults.value = items
        return items
    }

    fun isValid(): Boolean {
        return moduleId.isNotBlank() && moduleId.matches(Regex("^[a-zA-Z0-9_]+$")) &&
                moduleName.isNotBlank() &&
                moduleVersion.isNotBlank() && moduleVersion.matches(Regex("^[0-9.]+$"))
    }

    fun generateModuleProp(): String = builder.generateModuleProp()
    fun getTotalSize(): Long = builder.getTotalSize()
    fun buildAndSaveZip(context: Context): Uri? = builder.buildAndSaveZip(context)

    fun saveProject(context: Context, uri: Uri): Boolean = projectManager.saveProject(context, uri)
    fun loadProject(context: Context, uri: Uri): Boolean = projectManager.loadProject(context, uri)
    fun importModule(context: Context, uri: Uri): Boolean = importer.importModule(context, uri)

    fun applyTemplate(template: ModuleTemplate) = templateEngine.applyTemplate(template)
    fun resetToTemplate() = templateEngine.resetToTemplate()

    fun resetAll() {
        data.moduleId = ""
        data.moduleName = ""
        data.moduleVersion = ""
        data.moduleVersionCode = ""
        data.moduleAuthor = ""
        data.moduleLink = ""
        data.moduleDescription = ""
        data.moduleChangelog = ""
        data.customizeScript = ""
        data.serviceScript = ""
        data.postFsScript = ""
        data.updateBinaryType = "symlink"
        data.updateBinaryCustom = ""
        data.minMagisk = "20400"
        data.systemless = true
        data.needsystem = false
        data.skipMount = false
        data.recoveryMode = false
        data.replaceFiles = ""
        data.sepolicyRules = ""
        data.verifyKey = ""
        data.moduleFiles.clear()
    }

    fun fillDefaultCustomize() {
        val modId = moduleId.ifBlank { "my_module" }
        customizeScript = """#!/system/bin/sh
# This is the customize.sh script for $modId

ui_print "********************************"
ui_print "  $modId"
ui_print "  Installing..."
ui_print "********************************"

set_perm_recursive ${'$'}MODPATH 0 0 0755 0644

# cp -f ${'$'}MODPATH/system/bin/my_tool /system/bin/my_tool
# set_perm /system/bin/my_tool 0 0 0755

ui_print "Installation complete!"
"""
    }

    fun fillDefaultService() {
        serviceScript = """#!/system/bin/sh
# This is the service.sh script

while [ "\$(getprop sys.boot_completed)" != "1" ]; do
  sleep 1
done

# /system/bin/my_daemon &

exit 0
"""
    }

    fun fillDefaultPostFs() {
        postFsScript = """#!/system/bin/sh
# This script runs after /data is mounted

# mount -o rw,remount /system
# echo "127.0.0.1 example.com" >> /system/etc/hosts

exit 0
"""
    }

    fun insertSnippet(snippet: String, target: String) {
        when (target) {
            "customize" -> customizeScript += snippet
            "service" -> serviceScript += snippet
        }
    }

    fun addFileFromUri(context: Context, uri: Uri, originalName: String) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return
            val bytes = inputStream.readBytes()
            val isBinary = when {
                originalName.endsWith(".sh", true) -> false
                originalName.endsWith(".conf", true) || originalName.endsWith(".cfg", true) -> false
                else -> true
            }
            val permissions = if (originalName.endsWith(".sh", true)) "0755" else "0644"
            val type = when {
                originalName.endsWith(".sh", true) -> "script"
                originalName.endsWith(".conf", true) || originalName.endsWith(".cfg", true) -> "config"
                else -> "binary"
            }
            val displayName = originalName.substringAfterLast("/")
            if (isBinary) {
                moduleFiles.add(
                    ModuleFile(
                        name = displayName,
                        content = "[binary]",
                        permissions = permissions,
                        type = "binary",
                        isBinary = true,
                        binaryContent = bytes
                    )
                )
            } else {
                moduleFiles.add(
                    ModuleFile(
                        name = displayName,
                        content = String(bytes, Charsets.UTF_8),
                        permissions = permissions,
                        type = type
                    )
                )
            }
            Logger.logInfo("Файл добавлен: $displayName (тип: $type, права: $permissions)")
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка чтения файла: ${e.message}", Toast.LENGTH_SHORT).show()
            Logger.logError("Ошибка добавления файла $originalName: ${e.message}")
        }
    }

    fun saveModuleProp(context: Context): Uri? {
        return try {
            val prop = builder.generateModuleProp()
            val bytes = prop.toByteArray()
            val fileName = "module.prop"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                uri?.let { context.contentResolver.openOutputStream(it)?.use { os -> os.write(bytes) } }
                uri
            } else {
                @Suppress("DEPRECATION")
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = java.io.File(dir, fileName)
                file.writeBytes(bytes)
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка сохранения module.prop: ${e.message}", Toast.LENGTH_SHORT).show()
            Logger.logError("Ошибка сохранения module.prop: ${e.message}")
            null
        }
    }

    companion object {
        val SNIPPETS = mapOf(
            "customize" to listOf(
                "mount" to "\n# Монтирование раздела\nmount -o rw,remount /system",
                "copy_dir" to "\n# Копирование директории\ncopy_dir \$MODPATH/system \$SYSTEM",
                "chmod" to "\n# Установка прав доступа\nset_perm_recursive \$MODPATH/system 0 0 0755 0644",
                "setprop" to "\n# Установка свойства\nsetprop persist.my_module.enabled true",
                "ui_print" to "\n# Вывод в консоль Magisk\nui_print \"Installing my module...\""
            ),
            "service" to listOf(
                "while loop" to "\nwhile [ \"\$(getprop sys.boot_completed)\" != \"1\" ]; do\n  sleep 1\ndone",
                "sleep" to "\nsleep 5",
                "am" to "\nam start -n com.example/.MainActivity",
                "settings" to "\nsettings put global my_setting 1",
                "log" to "\necho \"Service started\" >> /data/local/tmp/log.txt"
            )
        )
    }
}