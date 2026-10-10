package com.devtools.utils.tools

class DuplicateRemoverTool {
    fun execute(input: String): String =
        LinkedHashSet(input.split('\n')).joinToString("\n")
}
