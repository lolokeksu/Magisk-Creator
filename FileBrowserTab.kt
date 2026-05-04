package com.magisk.next.ui.tabs

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magisk.next.AppSettings
import com.magisk.next.viewmodel.ModuleViewModel
import java.io.File

private val allHiddenExtensions = listOf(
    "zip", "7z", "rar", "tar", "gz", "bz2", "xz", "lz", "lz4", "lzma", "zst",
    "cab", "arj", "ace", "tgz", "tbz2", "txz", "tlz", "tlz4",
    "apk", "jar", "war", "ear", "sar",
    "tmp", "temp", "log", "bak", "db", "db-journal",
    "dex", "odex", "vdex",
    "png", "jpg", "jpeg", "gif", "webp", "bmp",
    "mp3", "ogg", "wav", "flac", "aac",
    "mp4", "mkv", "avi", "mov", "webm",
    "ttf", "otf",
    "txt", "md", "doc", "docx", "pdf", "xls", "xlsx", "ppt", "pptx",
    "pages", "numbers", "key",
    "csv", "json", "xml", "html", "htm", "css", "js",
    "iso", "img", "dmg", "vmdk", "vhd", "vhdx",
    "bin", "exe", "java", "env", "kt",
    "torrent", "magnet"
)

@Composable
fun FileBrowserTab(viewModel: ModuleViewModel) {
    val context = LocalContext.current
    // Убрано remember, значение всегда актуальное
    val settings = AppSettings(context)

    val rootDir = Environment.getExternalStorageDirectory().absolutePath
    var currentDir by remember { mutableStateOf(rootDir) }
    var sortAscending by remember { mutableStateOf(true) }
    var sortByDate by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    val hiddenExtensionsState = remember {
        val saved = settings.hiddenExtensions
        mutableStateMapOf<String, Boolean>().apply {
            saved.forEach { this[it] = true }
        }
    }

    val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        true
    }

    val files = if (hasPermission) {
        val list = File(currentDir).listFiles()
        list?.filter { f ->
            if (!settings.showHiddenFiles && f.name.startsWith(".")) return@filter false

            val ext = f.extension.lowercase()
            if (hiddenExtensionsState[ext] == true) return@filter false

            true
        }?.sortedWith { a, b ->
            if (a.isDirectory != b.isDirectory) {
                if (a.isDirectory) -1 else 1
            } else {
                val cmp = if (sortByDate) {
                    a.lastModified().compareTo(b.lastModified())
                } else {
                    a.name.lowercase().compareTo(b.name.lowercase())
                }
                if (sortAscending) cmp else -cmp
            }
        } ?: emptyList()
    } else {
        emptyList()
    }

    val cs = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            if (currentDir != rootDir) {
                IconButton(onClick = {
                    val parent = File(currentDir).parentFile
                    if (parent != null && parent.exists() && parent.absolutePath.startsWith(rootDir)) {
                        currentDir = parent.absolutePath
                    }
                }) {
                    Icon(Icons.Default.ArrowBack, null, tint = cs.onSurfaceVariant)
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
            Text(
                text = currentDir,
                color = cs.onSurface,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Настройки", tint = cs.primary)
                }
                DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Сортировка",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = cs.onSurfaceVariant
                            )
                        },
                        onClick = { }, enabled = false
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = !sortByDate, onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = cs.primary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "По имени",
                                    fontSize = 14.sp,
                                    color = if (!sortByDate) cs.primary else cs.onSurface
                                )
                            }
                        },
                        onClick = { sortByDate = false; showSortMenu = false }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = sortByDate, onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = cs.primary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("По дате", fontSize = 14.sp, color = if (sortByDate) cs.primary else cs.onSurface)
                            }
                        },
                        onClick = { sortByDate = true; showSortMenu = false }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Направление",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = cs.onSurfaceVariant
                            )
                        },
                        onClick = { }, enabled = false
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = sortAscending, onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = cs.primary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "По возрастанию ↑",
                                    fontSize = 14.sp,
                                    color = if (sortAscending) cs.primary else cs.onSurface
                                )
                            }
                        },
                        onClick = { sortAscending = true; showSortMenu = false }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = !sortAscending, onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = cs.primary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "По убыванию ↓",
                                    fontSize = 14.sp,
                                    color = if (!sortAscending) cs.primary else cs.onSurface
                                )
                            }
                        },
                        onClick = { sortAscending = false; showSortMenu = false }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Скрытые типы", fontSize = 14.sp) },
                        onClick = {
                            showSortMenu = false
                            showFilterDialog = true
                        },
                        leadingIcon = { Icon(Icons.Default.Filter, null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }
        }

        HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.2f))

        if (!hasPermission) {
            Box(modifier = Modifier.weight(1f).padding(16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Нет доступа", color = cs.onSurfaceVariant, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }) {
                        Text(text = "Настройки")
                    }
                }
            }
        } else if (files.isEmpty()) {
            Box(modifier = Modifier.weight(1f).padding(16.dp), contentAlignment = Alignment.Center) {
                Text(text = "Папка пуста", color = cs.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(files) { file ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            if (file.isDirectory) {
                                currentDir = file.absolutePath
                            }
                        }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val ic = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile
                        val tint = if (file.isDirectory) cs.tertiary else cs.onSurfaceVariant
                        Icon(ic, null, tint = tint)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = file.name, color = cs.onSurface, fontSize = 14.sp)
                            if (!file.isDirectory) {
                                val kb = file.length() / 1024
                                Text(text = "${kb} KB", color = cs.onSurface.copy(alpha = 0.6f), fontSize = 10.sp)
                            }
                        }
                        if (!file.isDirectory) {
                            IconButton(onClick = {
                                viewModel.addFileFromUri(context, Uri.fromFile(file), file.name)
                            }) {
                                Icon(Icons.Default.Add, null, tint = cs.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = {
                showFilterDialog = false
                settings.hiddenExtensions = hiddenExtensionsState.keys.toSet()
            },
            title = { Text("Скрываемые типы файлов") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(onClick = {
                            allHiddenExtensions.forEach { hiddenExtensionsState[it] = true }
                        }) {
                            Text("Выбрать всё")
                        }
                        TextButton(onClick = {
                            hiddenExtensionsState.clear()
                        }) {
                            Text("Отменить всё")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        allHiddenExtensions.forEach { ext ->
                            val isChecked = hiddenExtensionsState[ext] ?: false
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        hiddenExtensionsState[ext] = checked
                                        if (!checked) hiddenExtensionsState.remove(ext)
                                    }
                                )
                                Text(".$ext", fontSize = 14.sp, modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    settings.hiddenExtensions = hiddenExtensionsState.keys.toSet()
                    showFilterDialog = false
                }) {
                    Text("Закрыть")
                }
            }
        )
    }
}