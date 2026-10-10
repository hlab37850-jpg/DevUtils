package com.devtools.utils.tools

class GitignoreGeneratorTool {
    fun generateGitignore(projectType: String): String {
        val common = """
# OS and editor files
.DS_Store
Thumbs.db
.idea/
.vscode/
*.swp
*.swo
*~
.env
.env.*
!.env.example
""".trimIndent()
        val specific = when (projectType.trim().lowercase()) {
            "android" -> ".gradle/\n**/build/\nlocal.properties\n*.iml\n.externalNativeBuild/\n.cxx/\ncaptures/"
            "kotlin" -> ".gradle/\nbuild/\nout/\n*.class\n*.jar\n*.war\n*.ear\n.kotlin/"
            "java" -> "target/\nbuild/\n.gradle/\nout/\n*.class\n*.jar\n*.war\n*.ear\nhs_err_pid*"
            "python" -> "__pycache__/\n*.py[cod]\n*\$py.class\n.Python\nvenv/\n.venv/\nenv/\nENV/\nbuild/\ndist/\n*.egg-info/\n.pytest_cache/\n.mypy_cache/\n.ruff_cache/\n.coverage\nhtmlcov/"
            "node.js", "nodejs", "node" -> "node_modules/\nnpm-debug.log*\nyarn-debug.log*\npnpm-debug.log*\n.npm/\n.pnpm-store/\ncoverage/\ndist/\nbuild/\n.next/\n.nuxt/\n.cache/"
            "react" -> "node_modules/\nbuild/\ndist/\ncoverage/\n.env.local\n.env.development.local\n.env.test.local\n.env.production.local\n.next/\n.vite/"
            "flutter" -> ".dart_tool/\n.flutter-plugins\n.flutter-plugins-dependencies\n.packages\n.pub-cache/\n.pub/\nbuild/\ncoverage/\n.flutter/\nandroid/local.properties\nios/Flutter/.last_build_id"
            else -> throw IllegalArgumentException("نوع المشروع غير مدعوم.")
        }
        return "$common\n\n$specific\n"
    }
}
