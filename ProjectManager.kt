package com.magisk.next.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.magisk.next.Logger
import com.magisk.next.model.ModuleFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ProjectManager(private val data: ModuleData) {

    fun saveProject(context: Context, uri: Uri): Boolean {
        return try {
            val json = JSONObject().apply {
                put("moduleId", data.moduleId)
                put("moduleName", data.moduleName)
                put("moduleVersion", data.moduleVersion)
                put("moduleVersionCode", data.moduleVersionCode)
                put("moduleAuthor", data.moduleAuthor)
                put("moduleLink", data.moduleLink)
                put("moduleDescription", data.moduleDescription)
                put("moduleChangelog", data.moduleChangelog)
                put("customizeScript", data.customizeScript)
                put("serviceScript", data.serviceScript)
                put("postFsScript", data.postFsScript)
                put("updateBinaryType", data.updateBinaryType)
                put("updateBinaryCustom", data.updateBinaryCustom)
                put("minMagisk", data.minMagisk)
                put("systemless", data.systemless)
                put("needsystem", data.needsystem)
                put("skipMount", data.skipMount)
                put("recoveryMode", data.recoveryMode)
                put("replaceFiles", data.replaceFiles)
                put("sepolicyRules", data.sepolicyRules)
                put("verifyKey", data.verifyKey)
                put("files", JSONArray().apply {
                    data.moduleFiles.forEach { file ->
                        put(JSONObject().apply {
                            put("name", file.name)
                            put("content", file.content)
                            put("permissions", file.permissions)
                            put("type", file.type)
                        })
                    }
                })
            }
            val baos = ByteArrayOutputStream()
            ZipOutputStream(baos).use { zos ->
                zos.putNextEntry(ZipEntry("project.json"))
                zos.write(json.toString(2).toByteArray())
                zos.closeEntry()
            }
            context.contentResolver.openOutputStream(uri)?.use { it.write(baos.toByteArray()) }
                ?: throw Exception("Не удалось открыть файл для записи")
            Logger.logInfo("Проект сохранён: $uri")
            true
        } catch (e: Exception) {
            Logger.logError("Ошибка сохранения проекта $uri: ${e.message}")
            Toast.makeText(context, "Ошибка сохранения проекта: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            false
        }
    }

    fun loadProject(context: Context, uri: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val zipBytes = inputStream.readBytes()
            val zis = ZipInputStream(zipBytes.inputStream())
            var jsonString = ""
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name == "project.json") {
                    jsonString = zis.readBytes().toString(Charsets.UTF_8)
                    break
                }
                entry = zis.nextEntry
            }
            zis.close()
            if (jsonString.isNotEmpty()) {
                val json = JSONObject(jsonString)
                data.moduleId = json.optString("moduleId", "")
                data.moduleName = json.optString("moduleName", "")
                data.moduleVersion = json.optString("moduleVersion", "")
                data.moduleVersionCode = json.optString("moduleVersionCode", "")
                data.moduleAuthor = json.optString("moduleAuthor", "")
                data.moduleLink = json.optString("moduleLink", "")
                data.moduleDescription = json.optString("moduleDescription", "")
                data.moduleChangelog = json.optString("moduleChangelog", "")
                data.customizeScript = json.optString("customizeScript", "")
                data.serviceScript = json.optString("serviceScript", "")
                data.postFsScript = json.optString("postFsScript", "")
                data.updateBinaryType = json.optString("updateBinaryType", "symlink")
                data.updateBinaryCustom = json.optString("updateBinaryCustom", "")
                data.minMagisk = json.optString("minMagisk", "20400")
                data.systemless = json.optBoolean("systemless", true)
                data.needsystem = json.optBoolean("needsystem", false)
                data.skipMount = json.optBoolean("skipMount", false)
                data.recoveryMode = json.optBoolean("recoveryMode", false)
                data.replaceFiles = json.optString("replaceFiles", "")
                data.sepolicyRules = json.optString("sepolicyRules", "")
                data.verifyKey = json.optString("verifyKey", "")
                data.moduleFiles.clear()
                val filesArray = json.optJSONArray("files")
                if (filesArray != null) {
                    for (i in 0 until filesArray.length()) {
                        val fileObj = filesArray.getJSONObject(i)
                        data.moduleFiles.add(
                            ModuleFile(
                                name = fileObj.getString("name"),
                                content = fileObj.optString("content", ""),
                                permissions = fileObj.optString("permissions", "0644"),
                                type = fileObj.optString("type", "text")
                            )
                        )
                    }
                }
                Logger.logInfo("Проект загружен: $uri")
                true
            } else {
                val importer = ModuleImporter(data)
                val result = importer.importModule(context, uri)
                if (result) {
                    Logger.logInfo("Модуль импортирован через загрузку проекта: $uri")
                } else {
                    Logger.logWarning("Не удалось импортировать модуль через загрузку проекта: $uri")
                }
                result
            }
        } catch (e: Exception) {
            Logger.logError("Ошибка загрузки проекта $uri: ${e.message}")
            Toast.makeText(context, "Ошибка загрузки проекта: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            false
        }
    }
}