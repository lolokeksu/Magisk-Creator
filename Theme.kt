package com.magisk.next.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.magisk.next.AppSettings

// Стандартная тёмная палитра
val BgPrimaryDefault = Color(0xFF0A0E17)
val BgSecondaryDefault = Color(0xFF111827)
val BgTertiaryDefault = Color(0xFF1A2235)
val BorderDefault = Color(0xFF2A3A52)
val TextPrimaryDefault = Color(0xFFE2E8F0)
val TextSecondaryDefault = Color(0xFF94A3B8)
val TextMutedDefault = Color(0xFF64748B)
val Accent = Color(0xFF3B82F6)
val Success = Color(0xFF10B981)
val Warning = Color(0xFFF59E0B)
val Danger = Color(0xFFEF4444)
val Purple = Color(0xFF8B5CF6)

// AMOLED палитра
val BgPrimaryAmoled = Color(0xFF000000)
val BgSecondaryAmoled = Color(0xFF0A0A0A)
val BgTertiaryAmoled = Color(0xFF141414)
val BorderAmoled = Color(0xFF2A2A2A)
val TextPrimaryAmoled = Color(0xFFFFFFFF)
val TextSecondaryAmoled = Color(0xFFAAAAAA)
val TextMutedAmoled = Color(0xFF777777)

// Глобальные переменные, изменяемые в зависимости от темы
var BgPrimary by mutableStateOf(BgPrimaryDefault)
var BgSecondary by mutableStateOf(BgSecondaryDefault)
var BgTertiary by mutableStateOf(BgTertiaryDefault)
var Border by mutableStateOf(BorderDefault)
var TextPrimary by mutableStateOf(TextPrimaryDefault)
var TextSecondary by mutableStateOf(TextSecondaryDefault)
var TextMuted by mutableStateOf(TextMutedDefault)

@Composable
fun MagiskModuleBuilderTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val settings = remember { AppSettings(context) }
    val themeMode = settings.themeMode
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        "amoled" -> true
        else -> isSystemInDarkTheme()
    }

    // Обновляем глобальные цвета в зависимости от темы
    when (themeMode) {
        "amoled" -> {
            BgPrimary = BgPrimaryAmoled
            BgSecondary = BgSecondaryAmoled
            BgTertiary = BgTertiaryAmoled
            Border = BorderAmoled
            TextPrimary = TextPrimaryAmoled
            TextSecondary = TextSecondaryAmoled
            TextMuted = TextMutedAmoled
        }
        "light" -> {
            BgPrimary = Color(0xFFF8FAFC)
            BgSecondary = Color(0xFFFFFFFF)
            BgTertiary = Color(0xFFF1F5F9)
            Border = Color(0xFFE2E8F0)
            TextPrimary = Color(0xFF0F172A)
            TextSecondary = Color(0xFF334155)
            TextMuted = Color(0xFF64748B)
        }
        else -> {
            BgPrimary = BgPrimaryDefault
            BgSecondary = BgSecondaryDefault
            BgTertiary = BgTertiaryDefault
            Border = BorderDefault
            TextPrimary = TextPrimaryDefault
            TextSecondary = TextSecondaryDefault
            TextMuted = TextMutedDefault
        }
    }

    val colorScheme = when {
        dynamicColor && darkTheme -> dynamicDarkColorScheme(context)
        dynamicColor && !darkTheme -> dynamicLightColorScheme(context)
        darkTheme -> darkColorScheme(
            primary = Accent,
            secondary = TextSecondary,
            background = BgPrimary,
            surface = BgPrimaryAmoled, // исправлено для AMOLED
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onBackground = TextPrimary,
            onSurface = TextSecondary,
            error = Danger
        )
        else -> lightColorScheme(
            primary = Accent,
            secondary = Color(0xFF3B82F6),
            background = BgPrimary,
            surface = BgSecondary,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = TextPrimary,
            onSurface = TextSecondary,
            error = Danger
        )
    }

    MaterialTheme(colorScheme = colorScheme) {
        content()
    }
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onBackground,
    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = Border,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = TextMuted
)