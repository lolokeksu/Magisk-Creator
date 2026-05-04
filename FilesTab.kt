package com.magisk.next.ui.tabs

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magisk.next.R
import com.magisk.next.model.ModuleFile
import com.magisk.next.ui.theme.*
import com.magisk.next.viewmodel.ModuleViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesTab(
    viewModel: ModuleViewModel,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600
    val scope = rememberCoroutineScope()

    var fileName by remember { mutableStateOf("") }
    var fileContent by remember { mutableStateOf("") }
    var filePermissions by remember { mutableStateOf("0644") }
    var fileType by remember { mutableStateOf("text") }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    var typeExpanded by remember { mutableStateOf(false) }
    var permExpanded by remember { mutableStateOf(false) }

    val fileTypes = listOf(
        "text" to stringResource(R.string.file_type_text),
        "script" to stringResource(R.string.file_type_script),
        "binary" to stringResource(R.string.file_type_binary),
        "config" to stringResource(R.string.file_type_config)
    )
    val permissions = listOf(
        "0644" to "0644 - ${stringResource(R.string.permission_regular)}",
        "0755" to "0755 - ${stringResource(R.string.permission_executable)}",
        "0700" to "0700 - ${stringResource(R.string.permission_owner_only)}",
        "0600" to "0600 - ${stringResource(R.string.permission_read_write)}"
    )

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                viewModel.addFileFromUri(context, uri, uri.lastPathSegment ?: "unknown")
            }
            scope.launch {
                snackbarHostState.showSnackbar("Добавлено ${uris.size} файл(ов)")
            }
        }
    }

    fun addOrUpdateFile() {
        if (fileName.isBlank()) return
        val newFile = ModuleFile(
            name = fileName,
            content = fileContent,
            permissions = filePermissions,
            type = fileType
        )
        val index = editingIndex
        if (index != null && index in viewModel.moduleFiles.indices) {
            viewModel.moduleFiles[index] = newFile
            editingIndex = null
        } else {
            viewModel.moduleFiles.add(newFile)
        }
        fileName = ""
        fileContent = ""
        filePermissions = "0644"
        fileType = "text"
    }

    fun startEdit(index: Int) {
        val file = viewModel.moduleFiles[index]
        fileName = file.name
        fileContent = file.content
        filePermissions = file.permissions
        fileType = file.type
        editingIndex = index
    }

    if (isWideScreen) {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                FileAddForm(
                    fileName = fileName,
                    onFileNameChange = { fileName = it },
                    fileContent = fileContent,
                    onFileContentChange = { fileContent = it },
                    filePermissions = filePermissions,
                    onFilePermissionsChange = { filePermissions = it },
                    fileType = fileType,
                    onFileTypeChange = { fileType = it },
                    typeExpanded = typeExpanded,
                    onTypeExpandedChange = { typeExpanded = it },
                    permExpanded = permExpanded,
                    onPermExpandedChange = { permExpanded = it },
                    fileTypes = fileTypes,
                    permissions = permissions,
                    editingIndex = editingIndex,
                    onAddOrUpdate = { addOrUpdateFile() },
                    onPickFile = { filePickerLauncher.launch("*/*") }
                )
            }
            Column(modifier = Modifier.weight(2f).padding(start = 8.dp)) {
                FileListPanel(
                    files = viewModel.moduleFiles,
                    onEdit = { startEdit(it) },
                    onDelete = { viewModel.moduleFiles.removeAt(it) },
                    onClearAll = { viewModel.moduleFiles.clear() }
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            FileAddForm(
                fileName = fileName,
                onFileNameChange = { fileName = it },
                fileContent = fileContent,
                onFileContentChange = { fileContent = it },
                filePermissions = filePermissions,
                onFilePermissionsChange = { filePermissions = it },
                fileType = fileType,
                onFileTypeChange = { fileType = it },
                typeExpanded = typeExpanded,
                onTypeExpandedChange = { typeExpanded = it },
                permExpanded = permExpanded,
                onPermExpandedChange = { permExpanded = it },
                fileTypes = fileTypes,
                permissions = permissions,
                editingIndex = editingIndex,
                onAddOrUpdate = { addOrUpdateFile() },
                onPickFile = { filePickerLauncher.launch("*/*") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            FileListPanel(
                files = viewModel.moduleFiles,
                onEdit = { startEdit(it) },
                onDelete = { viewModel.moduleFiles.removeAt(it) },
                onClearAll = { viewModel.moduleFiles.clear() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileAddForm(
    fileName: String,
    onFileNameChange: (String) -> Unit,
    fileContent: String,
    onFileContentChange: (String) -> Unit,
    filePermissions: String,
    onFilePermissionsChange: (String) -> Unit,
    fileType: String,
    onFileTypeChange: (String) -> Unit,
    typeExpanded: Boolean,
    onTypeExpandedChange: (Boolean) -> Unit,
    permExpanded: Boolean,
    onPermExpandedChange: (Boolean) -> Unit,
    fileTypes: List<Pair<String, String>>,
    permissions: List<Pair<String, String>>,
    editingIndex: Int?,
    onAddOrUpdate: () -> Unit,
    onPickFile: () -> Unit
) {
    val highlighter = remember { ShellSyntaxHighlighter() }
    val applyHighlight = fileType == "script" || fileName.endsWith(".sh", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgSecondary),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.title_add_file), style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = fileName,
                onValueChange = onFileNameChange,
                label = { Text(stringResource(R.string.label_file_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = onTypeExpandedChange) {
                OutlinedTextField(
                    value = fileTypes.first { it.first == fileType }.second,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.label_file_type)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = textFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { onTypeExpandedChange(false) }
                ) {
                    fileTypes.forEach { (key, label) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = { onFileTypeChange(key) })
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(expanded = permExpanded, onExpandedChange = onPermExpandedChange) {
                OutlinedTextField(
                    value = permissions.first { it.first == filePermissions }.second,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.label_permissions)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = permExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = textFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = permExpanded,
                    onDismissRequest = { onPermExpandedChange(false) }
                ) {
                    permissions.forEach { (key, label) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = { onFilePermissionsChange(key) })
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = fileContent,
                onValueChange = onFileContentChange,
                label = { Text(stringResource(R.string.label_content)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                colors = textFieldColors(),
                visualTransformation = if (applyHighlight) highlighter else VisualTransformation.None
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAddOrUpdate,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Accent)
            ) {
                Text(
                    if (editingIndex != null) stringResource(R.string.btn_save_changes) else stringResource(R.string.btn_add_file),
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(onClick = onPickFile, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.UploadFile, contentDescription = null, tint = TextSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_upload_file), color = TextSecondary)
            }
        }
    }
}

@Composable
private fun FileListPanel(
    files: List<ModuleFile>,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onClearAll: () -> Unit
) {
    var deleteIndex by remember { mutableStateOf<Int?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgSecondary),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.title_file_structure), style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Spacer(modifier = Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(12.dp), color = Accent.copy(alpha = 0.15f)) {
                    Text(
                        stringResource(R.string.files_count, files.size),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                        color = Accent
                    )
                }
                if (files.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { showClearAllDialog = true }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(R.string.btn_clear_all), tint = Danger)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (files.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📂", fontSize = 32.sp)
                        Text(stringResource(R.string.no_files_added), color = TextMuted, fontSize = 14.sp)
                        Text(stringResource(R.string.hint_add_files), color = TextMuted, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
    modifier = Modifier.heightIn(max = 400.dp)  // ограничиваем высоту, чтобы скролл работал
) {
    itemsIndexed(files) { index, file ->
        FileItemWithPreview(
            file = file,
            onEdit = { onEdit(index) },
            onDeleteRequest = { deleteIndex = index }
        )
        HorizontalDivider(color = Border.copy(alpha = 0.2f))
    }
}
            }
        }
    }

    deleteIndex?.let { index ->
        AlertDialog(
            onDismissRequest = { deleteIndex = null },
            title = { Text(stringResource(R.string.dlg_delete_file_title)) },
            text = { Text(stringResource(R.string.dlg_delete_file_message, files[index].name)) },
            confirmButton = {
                TextButton(onClick = { onDelete(index); deleteIndex = null }) { Text(stringResource(R.string.dlg_delete_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteIndex = null }) { Text(stringResource(R.string.dlg_delete_cancel)) }
            }
        )
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text(stringResource(R.string.dlg_clear_all_title)) },
            text = { Text(stringResource(R.string.dlg_clear_all_message, files.size)) },
            confirmButton = {
                TextButton(onClick = { onClearAll(); showClearAllDialog = false }) { Text(stringResource(R.string.dlg_clear_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) { Text(stringResource(R.string.dlg_clear_cancel)) }
            }
        )
    }
}

@Composable
private fun FileItemWithPreview(
    file: ModuleFile,
    onEdit: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val icon = getFileIcon(file.name)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(file.name, color = TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                Row {
                    val permColor = if (file.permissions == "0755") Success else TextMuted
                    Text("${file.permissions} · ${file.type}", color = permColor, fontSize = 12.sp)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.btn_edit), tint = TextSecondary)
            }
            IconButton(onClick = onDeleteRequest) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.btn_delete), tint = Danger)
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(200)) + fadeIn(tween(200)),
            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(tween(200))
        ) {
            if (file.isBinary) {
                Text(
                    text = "[binary data]",
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp).fillMaxWidth(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            } else {
                Text(
                    text = file.content.ifBlank { "// empty" },
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp).fillMaxWidth(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

fun getFileIcon(fileName: String): String = when {
    fileName.endsWith(".sh") -> "📜"
    fileName.endsWith(".prop") -> "📄"
    fileName.endsWith(".xml") -> "📰"
    fileName.endsWith(".conf") || fileName.endsWith(".cfg") -> "⚙️"
    fileName.endsWith(".so") -> "🔧"
    fileName.endsWith(".bin") -> "💾"
    fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".svg") -> "🖼️"
    fileName.endsWith(".db") || fileName.endsWith(".sqlite") -> "🗃️"
    else -> "📄"
}