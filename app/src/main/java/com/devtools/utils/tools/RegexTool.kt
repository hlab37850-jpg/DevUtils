package com.devtools.utils.tools

import java.util.regex.Pattern

class RegexTool {
    fun execute(input: String, pattern: String): String {
        require(pattern.isNotEmpty()) { "أدخل نمط Regex أولاً." }
        val matcher = Pattern.compile(pattern).matcher(input)
        val result = StringBuilder()
        var count = 0
        while (matcher.find()) {
            count++
            result.append(count).append(". ").append(matcher.group())
                .append("\n   الموضع: ").append(matcher.start()).append("–").append(matcher.end()).append("\n")
        }
        return if (count == 0) "لا توجد تطابقات." else "عدد التطابقات: $count\n\n$result".trimEnd()
    }
}
