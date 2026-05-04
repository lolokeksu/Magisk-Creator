package com.magisk.next.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.magisk.next.Logger
import com.magisk.next.MainActivity
import com.magisk.next.R
import com.magisk.next.ui.tabs.*
import com.magisk.next.ui.theme.TextSecondary
import com.magisk.next.viewmodel.ModuleTemplate
import com.magisk.next.viewmodel.ModuleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Tab(val titleResId: Int, val icon: @Composable () -> Unit) {
    METADATA(R.string.tab_metadata, { Icon(Icons.Default.Info, contentDescription = null) }),
    FILES(R.string.tab_files, { Icon(Icons.Default.Folder, contentDescription = null) }),
    SCRIPTS(R.string.tab_scripts, { Icon(Icons.Default.Code, contentDescription = null) }),
    PREVIEW(R.string.tab_preview, { Icon(Icons.Default.Visibility, contentDescription = null) }),
    ADVANCED(R.string.tab_advanced, { Icon(Icons.Default.Settings, contentDescription = null) }),
    BROWSER(R.string.tab_browser, { Icon(Icons.Default.Search, contentDescription = null) })
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(viewModel: ModuleViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(Tab.METADATA) }
    val context = LocalContext.current
    val activity = context as? Activity
    var showMenu by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Лаунчер сохранения проекта
    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val success = withContext(Dispatchers.IO) {
                    viewModel.saveProject(context, uri)
                }
                if (success) {
                    Logger.logInfo("Проект сохранён: $uri")
                    Toast.makeText(context, context.getString(R.string.toast_project_saved), Toast.LENGTH_SHORT).show()
                } else {
                    Logger.logError("Ошибка сохранения проекта")
                    Toast.makeText(context, context.getString(R.string.toast_save_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Лаунчер загрузки проекта / импорта модуля
    val loadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val success = withContext(Dispatchers.IO) {
                    viewModel.loadProject(context, uri)
                }
                if (success) {
                    Logger.logInfo("Проект/модуль загружен: $uri")
                    Toast.makeText(context, context.getString(R.string.toast_project_loaded), Toast.LENGTH_SHORT).show()
                } else {
                    Logger.logError("Ошибка загрузки проекта/модуля")
                    Toast.makeText(context, context.getString(R.string.toast_load_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    BackHandler(enabled = true) {
        when {
            showSettings -> showSettings = false
            else -> showExitDialog = true
        }
    }

    if (showSettings) {
        SettingsScreen(onBack = { showSettings = false })
        return
    }

    val pagerState = rememberPagerState(pageCount = { Tab.entries.size })

    // Двусторонняя синхронизация: клик по табу -> свайп, свайп -> выделение таба
    LaunchedEffect(selectedTab) {
        pagerState.animateScrollToPage(selectedTab.ordinal)
    }
    LaunchedEffect(pagerState.currentPage) {
        selectedTab = Tab.entries[pagerState.currentPage]
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name),
                        style = TextStyle(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF3B82F6), Color(0xFF06B6D4))
                            )
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    TextButton(onClick = { viewModel.resetToTemplate() }) {
                        Text(stringResource(R.string.action_template), color = MaterialTheme.colorScheme.onSurface)
                    }
                    TextButton(onClick = { viewModel.resetAll() }) {
                        Text(stringResource(R.string.action_reset), color = MaterialTheme.colorScheme.onSurface)
                    }

                    // Кнопка «Экспорт»
                    Button(
                        onClick = {
                            if (!viewModel.isValid()) {
                                Toast.makeText(context, context.getString(R.string.validation_fix_errors), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                                try {
                                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Ваше устройство не поддерживает запрос. Включите разрешение вручную в настройках приложения.", Toast.LENGTH_LONG).show()
                                }
                                return@Button
                            }
                            isExporting = true
                            scope.launch {
                                val uri = withContext(Dispatchers.IO) {
                                    viewModel.buildAndSaveZip(context)
                                }
                                delay(600)
                                isExporting = false
                                if (uri != null) {
                                    Logger.logInfo("Модуль экспортирован: $uri")
                                    Toast.makeText(context, context.getString(R.string.toast_module_saved), Toast.LENGTH_SHORT).show()
                                } else {
                                    Logger.logError("Ошибка экспорта модуля")
                                    Toast.makeText(context, context.getString(R.string.toast_build_error), Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = !isExporting
                    ) {
                        Text(stringResource(R.string.action_export))
                    }

                    // Меню
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.action_more), tint = MaterialTheme.colorScheme.onSurface)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_save_project)) },
                                onClick = {
                                    showMenu = false
                                    try { saveLauncher.launch("project.mmproj") }
                                    catch (e: Exception) { Toast.makeText(context, context.getString(R.string.toast_file_manager_unavailable), Toast.LENGTH_SHORT).show() }
                                },
                                leadingIcon = { Icon(Icons.Default.Save, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_load_project)) },
                                onClick = {
                                    showMenu = false
                                    try { loadLauncher.launch(arrayOf("*/*")) }
                                    catch (e: Exception) { Toast.makeText(context, context.getString(R.string.toast_file_manager_unavailable), Toast.LENGTH_SHORT).show() }
                                },
                                leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_open_folder)) },
                                onClick = {
                                    showMenu = false
                                    try {
                                        val projectDir = java.io.File(Environment.getExternalStorageDirectory(), "MagiskModuleCreator/Project")
                                        if (!projectDir.exists()) projectDir.mkdirs()
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(
                                                androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", projectDir),
                                                "resource/folder"
                                            )
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, context.getString(R.string.toast_folder_open_error), Toast.LENGTH_SHORT).show()
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_settings)) },
                                onClick = {
                                    showMenu = false
                                    showSettings = true
                                },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.dlg_template_title)) },
                                onClick = { showMenu = false; showTemplateDialog = true },
                                leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                Tab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { selectedTab = tab },
                        text = { Text(stringResource(tab.titleResId)) },
                        icon = tab.icon,
                        selectedContentColor = Color(0xFF3B82F6), // синий акцент
                        unselectedContentColor = TextSecondary    // динамический цвет из темы
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                Box(modifier = Modifier.fillMaxSize()) {
                    when (page) {
                        Tab.METADATA.ordinal -> MetadataTab(viewModel)
                        Tab.FILES.ordinal -> FilesTab(viewModel)
                        Tab.SCRIPTS.ordinal -> ScriptsTab(viewModel)
                        Tab.PREVIEW.ordinal -> PreviewTab(viewModel)
                        Tab.ADVANCED.ordinal -> AdvancedTab(viewModel)
                        Tab.BROWSER.ordinal -> FileBrowserTab(viewModel)
                    }
                }
            }
        }
    }

    // Прогресс экспорта
    if (isExporting) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.export_progress_title)) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.export_progress_message))
                }
            },
            confirmButton = {}
        )
    }

    // Диалог шаблонов
    if (showTemplateDialog) {
        AlertDialog(
            onDismissRequest = { showTemplateDialog = false },
            title = { Text(stringResource(R.string.dlg_template_title)) },
            text = {
                Column {
                    Text("• ${stringResource(R.string.dlg_template_empty)}", modifier = Modifier.clickable {
                        viewModel.applyTemplate(ModuleTemplate.EMPTY)
                        showTemplateDialog = false
                    })
                    Text("• ${stringResource(R.string.dlg_template_debloater)}", modifier = Modifier.clickable {
                        viewModel.applyTemplate(ModuleTemplate.DEBLOATER)
                        showTemplateDialog = false
                    })
                    Text("• ${stringResource(R.string.dlg_template_hosts_blocker)}", modifier = Modifier.clickable {
                        viewModel.applyTemplate(ModuleTemplate.HOSTS_BLOCKER)
                        showTemplateDialog = false
                    })
                    Text("• ${stringResource(R.string.dlg_template_kernel_tweaks)}", modifier = Modifier.clickable {
                        viewModel.applyTemplate(ModuleTemplate.KERNEL_TWEAKS)
                        showTemplateDialog = false
                    })
                }
            },
            confirmButton = { TextButton(onClick = { showTemplateDialog = false }) { Text(stringResource(R.string.dlg_template_cancel)) } }
        )
    }

    // Диалог выхода
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.dlg_exit_title)) },
            text = { Text(stringResource(R.string.dlg_exit_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    try { saveLauncher.launch("project.mmproj") }
                    catch (e: Exception) { Toast.makeText(context, context.getString(R.string.toast_file_manager_unavailable), Toast.LENGTH_SHORT).show() }
                    activity?.finish()
                }) { Text(stringResource(R.string.dlg_exit_yes)) }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false; activity?.finish() }) { Text(stringResource(R.string.dlg_exit_no)) }
            }
        )
    }
}