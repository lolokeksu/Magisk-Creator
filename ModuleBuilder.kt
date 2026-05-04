package com.magisk.next.viewmodel

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.magisk.next.Logger
import com.magisk.next.model.ModuleFile
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ModuleBuilder(private val data: ModuleData) {

    fun generateModuleProp(): String = buildString {
        appendLine("id=${data.moduleId.ifBlank { "template_module" }}")
        appendLine("name=${data.moduleName.ifBlank { "Template Module" }}")
        appendLine("version=${data.moduleVersion.ifBlank { "1.0.0" }}")
        appendLine("versionCode=${data.moduleVersionCode.ifBlank { "1" }}")
        appendLine("author=${data.moduleAuthor.ifBlank { "Developer" }}")
        appendLine("description=${data.moduleDescription.ifBlank { "Description" }}")
        if (data.moduleLink.isNotBlank()) appendLine("link=${data.moduleLink}")
        if (data.moduleChangelog.isNotBlank()) appendLine("changelog=${data.moduleChangelog}")
        if (data.minMagisk.isNotBlank()) appendLine("minMagisk=${data.minMagisk}")
    }

    fun getTotalSize(): Long {
        var size = generateModuleProp().toByteArray().size.toLong()
        listOf(data.customizeScript, data.serviceScript, data.postFsScript, data.updateBinaryCustom).forEach {
            if (it.isNotBlank()) size += it.toByteArray().size
        }
        data.moduleFiles.forEach { size += it.content.toByteArray().size }
        return size
    }

    fun buildAndSaveZip(context: Context): Uri? {
        try {
            val tmpFile = File(context.cacheDir, "module_temp_${System.currentTimeMillis()}.zip")
            ZipOutputStream(tmpFile.outputStream()).use { zos ->
                zos.setLevel(0)
                val usedNames = mutableSetOf<String>()

                fun ZipOutputStream.safePutNextEntry(name: String): Boolean {
                    if (usedNames.add(name)) {
                        putNextEntry(ZipEntry(name))
                        return true
                    }
                    return false
                }

                // module.prop
                zos.safePutNextEntry("${data.moduleId}/module.prop")
                zos.write(generateModuleProp().toByteArray())
                zos.closeEntry()

                addScriptEntrySafe(zos, "customize.sh", data.customizeScript, usedNames)
                addScriptEntrySafe(zos, "service.sh", data.serviceScript, usedNames)
                addScriptEntrySafe(zos, "post-fs-data.sh", data.postFsScript, usedNames)

                // Флаги модуля
                if (data.systemless) {
                    zos.safePutNextEntry("${data.moduleId}/systemless")
                    zos.write(ByteArray(0))
                    zos.closeEntry()
                }
                if (data.needsystem) {
                    zos.safePutNextEntry("${data.moduleId}/needsystem")
                    zos.write(ByteArray(0))
                    zos.closeEntry()
                }
                if (data.skipMount) {
                    zos.safePutNextEntry("${data.moduleId}/skip_mount")
                    zos.write(ByteArray(0))
                    zos.closeEntry()
                }
                if (data.recoveryMode) {
                    zos.safePutNextEntry("${data.moduleId}/recovery_mode")
                    zos.write(ByteArray(0))
                    zos.closeEntry()
                }

                // replace-файлы
                val replacePaths = data.replaceFiles.lines().filter { it.isNotBlank() }.map { it.trim() }
                for (path in replacePaths) {
                    val entryName = "${data.moduleId}/system/$path"
                    if (zos.safePutNextEntry(entryName)) {
                        zos.write("# placeholder for $path".toByteArray())
                        zos.closeEntry()
                    }
                }

                if (data.sepolicyRules.isNotBlank()) {
                    if (zos.safePutNextEntry("${data.moduleId}/sepolicy.rules")) {
                        zos.write(data.sepolicyRules.toByteArray())
                        zos.closeEntry()
                    }
                }

                if (data.verifyKey.isNotBlank()) {
                    if (zos.safePutNextEntry("${data.moduleId}/verify")) {
                        zos.write(data.verifyKey.toByteArray())
                        zos.closeEntry()
                    }
                }

                // META-INF
                if (zos.safePutNextEntry("${data.moduleId}/META-INF/com/google/android/update-binary")) {
                    zos.write(ByteArray(0))
                    zos.closeEntry()
                }
                if (zos.safePutNextEntry("${data.moduleId}/META-INF/com/google/android/updater-script")) {
                    zos.write("#MAGISK\n".toByteArray())
                    zos.closeEntry()
                }

                for (file in data.moduleFiles) {
                    if (file.name.endsWith("/")) continue
                    val entryName = "${data.moduleId}/${file.name}"
                    if (zos.safePutNextEntry(entryName)) {
                        val fileData = if (file.isBinary && file.binaryContent != null) {
                            file.binaryContent!!
                        } else {
                            file.content.toByteArray()
                        }
                        zos.write(fileData)
                        zos.closeEntry()
                    }
                }
            }

            // Перемещаем временный файл в целевую папку
            val fileName = "${data.moduleId}-${data.moduleVersion.ifBlank { "1.0.0" }}.zip"
            val appDir = File(Environment.getExternalStorageDirectory(), "MagiskModuleCreator")
            if (!appDir.exists()) appDir.mkdirs()
            val projectDir = File(appDir, "Project")
            if (!projectDir.exists()) projectDir.mkdirs()

            val finalFile = File(projectDir, fileName)
            tmpFile.copyTo(finalFile, overwrite = true)
            tmpFile.delete()

            Logger.logInfo("Модуль экспортирован: ${finalFile.absolutePath}")
            return Uri.fromFile(finalFile)
        } catch (e: Exception) {
            Logger.logError("Ошибка экспорта модуля: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    private fun addScriptEntrySafe(
        zos: ZipOutputStream,
        name: String,
        content: String,
        usedNames: MutableSet<String>
    ) {
        if (content.isNotBlank()) {
            val entryName = "${data.moduleId}/$name"
            if (usedNames.add(entryName)) {
                zos.putNextEntry(ZipEntry(entryName))
                zos.write(content.toByteArray())
                zos.closeEntry()
            }
        }
    }
}