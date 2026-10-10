package com.devtools.utils.tools

class CommitMessageTool {
    private val supportedTypes = setOf("feat", "fix", "docs", "style", "refactor", "test", "chore")
    fun generateCommitMessage(type: String, scope: String?, description: String, body: String?): String {
        val normalizedType = type.trim().lowercase()
        require(normalizedType in supportedTypes) { "نوع Commit غير مدعوم." }
        val cleanDescription = description.trim().replace(Regex("\\s+"), " ")
        require(cleanDescription.isNotEmpty()) { "أدخل وصفاً مختصراً للتغيير." }
        require(!cleanDescription.contains('\n') && !cleanDescription.contains('\r')) { "الوصف المختصر يجب أن يكون في سطر واحد." }
        val cleanScope = scope?.trim()?.takeIf { it.isNotEmpty() }
        require(cleanScope == null || (!cleanScope.contains('(') && !cleanScope.contains(')') && !cleanScope.contains('\n'))) { "قيمة Scope غير صالحة." }
        val header = if (cleanScope == null) "$normalizedType: $cleanDescription" else "$normalizedType($cleanScope): $cleanDescription"
        val cleanBody = body?.trim().orEmpty()
        return if (cleanBody.isEmpty()) header else "$header\n\n$cleanBody"
    }
}
