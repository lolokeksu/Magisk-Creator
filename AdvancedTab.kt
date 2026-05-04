package com.magisk.next.ui.tabs

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magisk.next.R
import com.magisk.next.ui.theme.*
import com.magisk.next.viewmodel.ModuleViewModel

@Composable
fun AdvancedTab(viewModel: ModuleViewModel) {
    val configuration = LocalConfiguration.current
    val isWide = configuration.screenWidthDp >= 600

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (isWide) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    CollapsibleCard(title = stringResource(R.string.title_options), initiallyExpanded = false) {
                        OptionsContent(viewModel)
                    }
                }
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    CollapsibleCard(title = stringResource(R.string.title_special_files), initiallyExpanded = false) {
                        SpecialFilesContent(viewModel)
                    }
                }
            }
        } else {
            CollapsibleCard(title = stringResource(R.string.title_options), initiallyExpanded = false) {
                OptionsContent(viewModel)
            }
            Spacer(modifier = Modifier.height(12.dp))
            CollapsibleCard(title = stringResource(R.string.title_special_files), initiallyExpanded = false) {
                SpecialFilesContent(viewModel)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        CollapsibleCard(title = stringResource(R.string.title_summary), initiallyExpanded = false) {
            SummaryContent(viewModel)
        }
    }
}

@Composable
private fun CollapsibleCard(
    title: String,
    initiallyExpanded: Boolean = true,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

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
                Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) stringResource(R.string.collapse_section) else stringResource(R.string.expand_section),
                    tint = TextSecondary
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(tween(300))
            ) {
                content()
            }
        }
    }
}

@Composable
private fun OptionsContent(viewModel: ModuleViewModel) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
        OutlinedTextField(
            value = viewModel.minMagisk,
            onValueChange = { viewModel.minMagisk = it },
            label = { Text(stringResource(R.string.label_min_magisk)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors()
        )
        Spacer(modifier = Modifier.height(16.dp))
        SwitchOption(stringResource(R.string.option_systemless), stringResource(R.string.desc_systemless), viewModel.systemless) {
            viewModel.systemless = it
        }
        SwitchOption(stringResource(R.string.option_needsystem), stringResource(R.string.desc_needsystem), viewModel.needsystem) {
            viewModel.needsystem = it
        }
        SwitchOption(stringResource(R.string.option_skip_mount), stringResource(R.string.desc_skip_mount), viewModel.skipMount) {
            viewModel.skipMount = it
        }
        SwitchOption(stringResource(R.string.option_recovery_mode), stringResource(R.string.desc_recovery_mode), viewModel.recoveryMode) {
            viewModel.recoveryMode = it
        }
    }
}

@Composable
private fun SwitchOption(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(description, color = TextMuted, fontSize = 12.sp)
        }
        GradientSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SpecialFilesContent(viewModel: ModuleViewModel) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
        OutlinedTextField(
            value = viewModel.replaceFiles,
            onValueChange = { viewModel.replaceFiles = it },
            label = { Text(stringResource(R.string.label_replace)) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
            colors = textFieldColors()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(stringResource(R.string.hint_replace), color = TextMuted, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.sepolicyRules,
            onValueChange = { viewModel.sepolicyRules = it },
            label = { Text(stringResource(R.string.label_sepolicy)) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
            colors = textFieldColors()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(stringResource(R.string.hint_sepolicy), color = TextMuted, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.verifyKey,
            onValueChange = { viewModel.verifyKey = it },
            label = { Text(stringResource(R.string.label_verify)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(stringResource(R.string.hint_verify), color = TextMuted, fontSize = 12.sp)
    }
}

@Composable
private fun SummaryContent(viewModel: ModuleViewModel) {
    val totalFiles = viewModel.moduleFiles.size
    val scriptCount = listOf(viewModel.customizeScript, viewModel.serviceScript, viewModel.postFsScript).count { it.isNotBlank() }
    val totalSize = viewModel.getTotalSize()
    val version = viewModel.moduleVersion.ifBlank { "-" }

    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            SummaryItem(stringResource(R.string.summary_files), totalFiles.toString(), Accent)
            SummaryItem(stringResource(R.string.summary_scripts), scriptCount.toString(), Success)
            SummaryItem(stringResource(R.string.summary_size), formatSize(totalSize), Purple)
            SummaryItem(stringResource(R.string.summary_version), version, Warning)
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextMuted, fontSize = 12.sp)
    }
}

@Composable
private fun GradientSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            uncheckedThumbColor = Color.White,
            checkedTrackColor = Color.Transparent,
            uncheckedTrackColor = Color.Transparent,
            checkedBorderColor = Color.Transparent,
            uncheckedBorderColor = Color.Transparent
        )
    )
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return "%.1f %s".format(bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}