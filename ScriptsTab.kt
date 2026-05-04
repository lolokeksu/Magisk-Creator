package com.magisk.next.ui.tabs

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magisk.next.R
import com.magisk.next.ui.theme.*
import com.magisk.next.viewmodel.ModuleViewModel

@Composable
fun ScriptsTab(viewModel: ModuleViewModel) {
    val highlighter = remember { ShellSyntaxHighlighter() }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        CollapsibleScriptCard(
            name = "customize.sh", required = true,
            script = viewModel.customizeScript,
            onScriptChange = { viewModel.customizeScript = it },
            onFillDefault = { viewModel.fillDefaultCustomize() },
            snippetTarget = "customize", viewModel = viewModel,
            visualTransformation = highlighter
        )
        Spacer(modifier = Modifier.height(12.dp))

        CollapsibleScriptCard(
            name = "service.sh", required = false,
            script = viewModel.serviceScript,
            onScriptChange = { viewModel.serviceScript = it },
            onFillDefault = { viewModel.fillDefaultService() },
            snippetTarget = "service", viewModel = viewModel,
            visualTransformation = highlighter
        )
        Spacer(modifier = Modifier.height(12.dp))

        CollapsibleScriptCard(
            name = "post-fs-data.sh", required = false,
            script = viewModel.postFsScript,
            onScriptChange = { viewModel.postFsScript = it },
            onFillDefault = { viewModel.fillDefaultPostFs() },
            snippetTarget = null, viewModel = null,
            visualTransformation = highlighter
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Border.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))

        UpdateBinarySection(viewModel)
    }
}

@Composable
private fun CollapsibleScriptCard(
    name: String, required: Boolean, script: String,
    onScriptChange: (String) -> Unit, onFillDefault: () -> Unit,
    snippetTarget: String?, viewModel: ModuleViewModel?,
    visualTransformation: VisualTransformation
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgSecondary),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tagColor = if (required) Success else Accent
                Surface(shape = RoundedCornerShape(12.dp), color = tagColor.copy(alpha = 0.15f)) {
                    Text(
                        if (required) stringResource(R.string.label_required) else stringResource(R.string.label_optional),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 11.sp, color = tagColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(name, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onFillDefault) {
                    Text(stringResource(R.string.btn_fill_default), fontSize = 12.sp, color = TextSecondary)
                }
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) stringResource(R.string.collapse_script) else stringResource(R.string.expand_script),
                    tint = TextSecondary
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(tween(300))
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(BgTertiary, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                            Text(name, color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    // Редактор с номерами строк
                    // Редактор с номерами строк
val lines = script.lines()
val scrollState = rememberScrollState()

Box(
    modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 180.dp, max = 400.dp)
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Номера строк
        Column(
            modifier = Modifier
                .width(36.dp)
                .verticalScroll(scrollState)
                .padding(top = 12.dp, bottom = 12.dp)
        ) {
            lines.forEachIndexed { index, _ ->
                Text(
                    text = "${index + 1}",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = TextMuted,
                        lineHeight = 20.sp
                    )
                )
            }
        }
        // Поле ввода со встроенной прокруткой (синхронизировано с номерами)
        OutlinedTextField(
            value = script,
            onValueChange = onScriptChange,
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            ),
            colors = textFieldColors(),
            visualTransformation = visualTransformation
        )
    }
}

                    if (snippetTarget != null && viewModel != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val snippets = ModuleViewModel.SNIPPETS[snippetTarget] ?: emptyList()
                        val rows = snippets.chunked(3)
                        for (row in rows) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                for ((label, code) in row) {
                                    OutlinedButton(
                                        onClick = { viewModel.insertSnippet(code, snippetTarget) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                                    ) { Text(label, fontSize = 11.sp) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateBinarySection(viewModel: ModuleViewModel) {
    Text(stringResource(R.string.title_update_binary), style = MaterialTheme.typography.titleSmall, color = TextPrimary)
    Spacer(modifier = Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = viewModel.updateBinaryType == "symlink", onClick = { viewModel.updateBinaryType = "symlink" })
        Text(stringResource(R.string.label_symlink_recommended), color = TextSecondary)
        Spacer(modifier = Modifier.width(16.dp))
        RadioButton(selected = viewModel.updateBinaryType == "custom", onClick = { viewModel.updateBinaryType = "custom" })
        Text(stringResource(R.string.label_custom), color = TextSecondary)
    }
    if (viewModel.updateBinaryType == "custom") {
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(BgTertiary, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                Text("update-binary", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }
        OutlinedTextField(
            value = viewModel.updateBinaryCustom,
            onValueChange = { viewModel.updateBinaryCustom = it },
            modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
            colors = textFieldColors()
        )
    } else {
        Surface(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), shape = RoundedCornerShape(8.dp), color = BgTertiary) {
            Text(stringResource(R.string.using_symlink), modifier = Modifier.padding(12.dp), color = TextSecondary, fontSize = 13.sp)
        }
    }
}