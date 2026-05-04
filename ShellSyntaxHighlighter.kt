package com.magisk.next.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class ShellSyntaxHighlighter : VisualTransformation {
    // Базовые цвета (можно подставить из вашей темы)
    private val keywordColor = Color(0xFFFF79C6)   // розовый
    private val stringColor = Color(0xFFF1FA8C)    // жёлтый
    private val commentColor = Color(0xFF6272A4)   // серый
    private val operatorColor = Color(0xFFFF79C6)
    private val variableColor = Color(0xFF8BE9FD)  // голубой

    override fun filter(text: AnnotatedString): TransformedText {
        val builder = AnnotatedString.Builder(text.text)
        // Простая реализация: подсвечиваем строки после #
        val lines = text.text.lines()
        var currentIndex = 0
        for (line in lines) {
            val trimmed = line.trimStart()
            if (trimmed.startsWith("#")) {
                // Комментарий
                val start = currentIndex + line.indexOf("#")
                builder.addStyle(
                    SpanStyle(color = commentColor, fontStyle = FontStyle.Italic),
                    start,
                    currentIndex + line.length
                )
            }
            // Подсветка ключевых слов
            highlightKeywords(line, builder, currentIndex)
            currentIndex += line.length + 1 // +1 для перевода строки
        }
        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }

    private fun highlightKeywords(line: String, builder: AnnotatedString.Builder, lineOffset: Int) {
        val keywords = listOf(
            "ui_print", "set_perm", "set_perm_recursive", "chmod", "chown",
            "mount", "umount", "cp", "mv", "mkdir", "rm", "echo", "exit",
            "if", "then", "else", "elif", "fi", "while", "do", "done",
            "for", "in", "case", "esac", "function", "return", "sleep",
            "log", "abort", "getprop", "setprop", "resetprop"
        )
        val operators = listOf("&&", "||", "|", "&", ">>", "<<", ">", "<")
        val varPattern = Regex("\\\$[a-zA-Z_][a-zA-Z0-9_]*")
        val stringPattern = Regex("\"[^\"]*\"|'[^']*'")

        // Поиск и подсветка ключевых слов
        val lowerLine = line.lowercase()
        for (kw in keywords) {
            var startIndex = lowerLine.indexOf(kw)
            while (startIndex != -1) {
                val endIndex = startIndex + kw.length
                // проверка границ слова
                val isWord = (startIndex == 0 || !line[startIndex-1].isLetterOrDigit()) &&
                             (endIndex == line.length || !line[endIndex].isLetterOrDigit())
                if (isWord) {
                    builder.addStyle(
                        SpanStyle(color = keywordColor, fontWeight = FontWeight.Bold),
                        lineOffset + startIndex,
                        lineOffset + endIndex
                    )
                }
                startIndex = lowerLine.indexOf(kw, endIndex)
            }
        }
        // Переменные
        for (match in varPattern.findAll(line)) {
            builder.addStyle(
                SpanStyle(color = variableColor),
                lineOffset + match.range.first,
                lineOffset + match.range.last + 1
            )
        }
        // Строки
        for (match in stringPattern.findAll(line)) {
            builder.addStyle(
                SpanStyle(color = stringColor),
                lineOffset + match.range.first,
                lineOffset + match.range.last + 1
            )
        }
    }
}