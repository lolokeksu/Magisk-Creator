package com.magisk.next.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.magisk.next.Logger
import com.magisk.next.model.ModuleFile
import java.util.zip.ZipInputStream

class ModuleImporter(private val data: ModuleData) {

    fun importModule(context: Context, uri: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val zis = ZipInputStream(inputStream)
            var entry = zis.nextEntry
            val scriptContents = mutableMapOf<String, String>()
            val files = mutableListOf<ModuleFile>()

            var insideFolder = false

            while (entry != null) {
                val name = entry.name
                if (name.contains("META-INF") || entry.isDirectory) {
                    entry = zis.nextEntry
                    continue
                }

                val parts = name.split("/").filter { it.isNotBlank() }
                val relativePath: String

                if (name == "module.prop" || name.endsWith("/module.prop")) {
                    insideFolder = name.contains("/")
                }

                if (insideFolder) {
                    relativePath = if (parts.size > 1) parts.drop(1).joinToString("/") else ""
                } else {
                    relativePath = if (parts.size > 1) parts.joinToString("/") else parts.last()
                }

                if (relativePath.isEmpty()) {
                    entry = zis.nextEntry
                    continue
                }

                val fileName = parts.last()
                val content = zis.readBytes().toString(Charsets.UTF_8)

                when {
                    fileName == "module.prop" -> {
                        val props = parseModuleProp(content)
                        data.moduleId = props["id"] ?: ""
                        data.moduleName = props["name"] ?: ""
                        data.moduleVersion = props["version"] ?: ""
                        data.moduleVersionCode = props["versionCode"] ?: ""
                        data.moduleAuthor = props["author"] ?: ""
                        data.moduleLink = props["link"] ?: ""
                        data.moduleDescription = props["description"] ?: ""
                        data.moduleChangelog = props["changelog"] ?: ""
                        data.minMagisk = props["minMagisk"] ?: ""
                    }
                    fileName == "customize.sh" -> scriptContents["customize"] = content
                    fileName == "service.sh" -> scriptContents["service"] = content
                    fileName == "post-fs-data.sh" -> scriptContents["post-fs-data"] = content
                    fileName.startsWith(".minMagisk") -> data.minMagisk = content.trim()
                    else -> {
                        val isScript = fileName.endsWith(".sh")
                        files.add(
                            ModuleFile(
                                name = relativePath,
                                content = content,
                                permissions = if (isScript) "0755" else "0644",
                                type = if (isScript) "script" else "text"
                            )
                        )
                    }
                }
                entry = zis.nextEntry
            }
            zis.close()

            data.customizeScript = scriptContents["customize"] ?: ""
            data.serviceScript = scriptContents["service"] ?: ""
            data.postFsScript = scriptContents["post-fs-data"] ?: ""
            data.moduleFiles.clear()
            data.moduleFiles.addAll(files)

            Logger.logInfo("Модуль импортирован: $uri (файлов: ${files.size})")
            true
        } catch (e: Exception) {
            Logger.logError("Ошибка импорта модуля $uri: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    private fun parseModuleProp(content: String): Map<String, String> {
        val props = mutableMapOf<String, String>()
        content.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.contains("=") && !trimmed.startsWith("#")) {
                val key = trimmed.substringBefore("=").trim()
                val value = trimmed.substringAfter("=").trim()
                props[key] = value
            }
        }
        return props
    }
}