package com.devtools.utils.tools

import java.util.Locale

class CaseConverterTool {
    fun execute(input: String, mode: String): String {
        val words = input
            .replace(Regex("([a-z0-9])([A-Z])"), "\\$1 \\$2")
            .split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.isNotBlank() }
            .map { it.lowercase(Locale.ROOT) }

        if (words.isEmpty()) return ""

        return when (mode) {
            CAMEL_CASE -> words.first() + words.drop(1).joinToString("") { capitalize(it) }
            SNAKE_CASE -> words.joinToString("_")
            PASCAL_CASE -> words.joinToString("") { capitalize(it) }
            else -> throw IllegalArgumentException("نوع التحويل غير مدعوم.")
        }
    }

    private fun capitalize(word: String): String =
        word.replaceFirstChar { it.titlecase(Locale.ROOT) }

    companion object {
        const val CAMEL_CASE = "camelCase"
        const val SNAKE_CASE = "snake_case"
        const val PASCAL_CASE = "PascalCase"
    }
}
