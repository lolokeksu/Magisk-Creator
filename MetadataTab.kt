package com.magisk.next.ui.tabs

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.magisk.next.R
import com.magisk.next.ui.theme.textFieldColors
import com.magisk.next.viewmodel.ModuleViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MetadataTab(viewModel: ModuleViewModel) {

    var idTouched by remember { mutableStateOf(false) }
    var nameTouched by remember { mutableStateOf(false) }
    var versionTouched by remember { mutableStateOf(false) }

    // Для debounce-задержек
    val scope = rememberCoroutineScope()
    var idDebounceJob by remember { mutableStateOf<Job?>(null) }
    var versionDebounceJob by remember { mutableStateOf<Job?>(null) }

    fun idError(): Boolean {
        val v = viewModel.moduleId
        return v.isNotBlank() && !v.matches(Regex("^[a-zA-Z0-9_]+$"))
    }
    fun nameError(): Boolean = false
    fun versionError(): Boolean {
        val v = viewModel.moduleVersion
        return v.isNotBlank() && !v.matches(Regex("^[0-9.]+$"))
    }

    fun onIdChange(newValue: String) {
        viewModel.moduleId = newValue
        idDebounceJob?.cancel()
        idDebounceJob = scope.launch {
            delay(300)
            idTouched = newValue.isNotEmpty()
        }
    }

    fun onNameChange(newValue: String) {
        viewModel.moduleName = newValue
        nameTouched = newValue.isNotEmpty()
    }

    fun onVersionChange(newValue: String) {
        viewModel.moduleVersion = newValue
        versionDebounceJob?.cancel()
        versionDebounceJob = scope.launch {
            delay(300)
            versionTouched = newValue.isNotEmpty()
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = tween(200))
        ) {
            OutlinedTextField(
                value = viewModel.moduleId,
                onValueChange = { onIdChange(it) },
                label = { Text(stringResource(R.string.label_module_id)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(),
                isError = idTouched && idError(),
                supportingText = if (idTouched && idError()) {
                    { Text(stringResource(R.string.validation_error_id)) }
                } else null
            )
        }
        Spacer(Modifier.height(8.dp))

        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = tween(300))
        ) {
            OutlinedTextField(
                value = viewModel.moduleName,
                onValueChange = { onNameChange(it) },
                label = { Text(stringResource(R.string.label_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(),
                isError = nameTouched && nameError(),
                supportingText = if (nameTouched && nameError()) {
                    { Text(stringResource(R.string.validation_error_name)) }
                } else null
            )
        }
        Spacer(Modifier.height(8.dp))

        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = tween(400))
        ) {
            OutlinedTextField(
                value = viewModel.moduleVersion,
                onValueChange = { onVersionChange(it) },
                label = { Text(stringResource(R.string.label_version)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(),
                isError = versionTouched && versionError(),
                supportingText = if (versionTouched && versionError()) {
                    { Text(stringResource(R.string.validation_error_version)) }
                } else null
            )
        }
        Spacer(Modifier.height(8.dp))

        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = tween(500))
        ) {
            OutlinedTextField(
                value = viewModel.moduleVersionCode,
                onValueChange = { viewModel.moduleVersionCode = it },
                label = { Text(stringResource(R.string.label_version_code)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
        }
        Spacer(Modifier.height(8.dp))

        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = tween(600))
        ) {
            OutlinedTextField(
                value = viewModel.moduleAuthor,
                onValueChange = { viewModel.moduleAuthor = it },
                label = { Text(stringResource(R.string.label_author)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
        }
        Spacer(Modifier.height(8.dp))

        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = tween(700))
        ) {
            OutlinedTextField(
                value = viewModel.moduleLink,
                onValueChange = { viewModel.moduleLink = it },
                label = { Text(stringResource(R.string.label_link)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
        }
        Spacer(Modifier.height(8.dp))

        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = tween(800))
        ) {
            OutlinedTextField(
                value = viewModel.moduleDescription,
                onValueChange = { viewModel.moduleDescription = it },
                label = { Text(stringResource(R.string.label_description)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp),
                colors = textFieldColors()
            )
        }
        Spacer(Modifier.height(8.dp))

        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = tween(900))
        ) {
            OutlinedTextField(
                value = viewModel.moduleChangelog,
                onValueChange = { viewModel.moduleChangelog = it },
                label = { Text(stringResource(R.string.label_changelog)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                colors = textFieldColors()
            )
        }
    }
}