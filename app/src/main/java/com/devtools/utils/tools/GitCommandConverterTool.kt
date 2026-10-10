package com.devtools.utils.tools

class GitCommandConverterTool {
    private val shells = setOf("bash", "powershell", "fish")
    fun convertCommand(command: String, fromShell: String, toShell: String): String {
        val source = fromShell.trim().lowercase()
        val target = toShell.trim().lowercase()
        require(source in shells && target in shells) { "اختر Bash أو PowerShell أو Fish." }
        val input = command.trim()
        require(input.isNotEmpty()) { "أدخل أمراً للتحويل." }
        if (source == target) return input

        val exportMatch = Regex("""^export\s+([A-Za-z_][A-Za-z0-9_]*)=(.*)$""").matchEntire(input)
        if (exportMatch != null) {
            val name = exportMatch.groupValues[1]
            val value = unquote(exportMatch.groupValues[2])
            return when (target) {
                "powershell" -> "$" + "env:$name = \"$value\""
                "fish" -> "set -gx $name \"$value\""
                else -> "export $name=\"$value\""
            }
        }
        val assignment = Regex("""^\${'
        if (assignment != null) {
            val name = assignment.groupValues[1]
            val value = unquote(assignment.groupValues[2])
            return when (target) {
                "bash" -> "export $name=\"$value\""
                "fish" -> "set -gx $name \"$value\""
                else -> input
            }
        }
        val fishSet = Regex("""^set\s+-g?x\s+([A-Za-z_][A-Za-z0-9_]*)\s+(.+)$""").matchEntire(input)
        if (fishSet != null && source == "fish") {
            val name = fishSet.groupValues[1]
            val value = unquote(fishSet.groupValues[2])
            return when (target) {
                "bash" -> "export $name=\"$value\""
                "powershell" -> "$" + "env:$name = \"$value\""
                else -> input
            }
        }
        val args = input.split(Regex("\\s+"), limit = 2)
        val supported = input == "ls -la" || input == "ls -l" || input == "ls" || input == "pwd" ||
            input == "clear" || input.startsWith("cat ") || input.startsWith("mkdir ") ||
            input.startsWith("rm -rf ") || input.startsWith("git ")
        require(supported) { "هذا الأمر غير مدعوم للتحويل حالياً." }
        return when (args[0]) {
            "ls" -> when {
                input == "ls -la" && target == "powershell" -> "Get-ChildItem -Force"
                input == "ls -l" && target == "powershell" -> "Get-ChildItem"
                target == "powershell" -> "Get-ChildItem"
                else -> input
            }
            "cat" -> if (target == "powershell") "Get-Content ${args.getOrElse(1) { "" }}" else input
            "pwd" -> if (target == "powershell") "Get-Location" else input
            "clear" -> if (target == "powershell") "Clear-Host" else input
            "mkdir" -> if (target == "powershell") "New-Item -ItemType Directory -Name ${args.getOrElse(1) { "" }}" else input
            "rm" -> if (target == "powershell") "Remove-Item -Recurse -Force ${args.getOrElse(1) { "" }}" else input
            "git" -> input
            else -> input
        }
    }
    private fun unquote(value: String): String = value.trim().removeSurrounding("\"").removeSurrounding("'")
}
}env:([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*)$""").matchEntire(input)
        if (assignment != null) {
            val name = assignment.groupValues[1]
            val value = unquote(assignment.groupValues[2])
            return when (target) {
                "bash" -> "export $name=\"$value\""
                "fish" -> "set -gx $name \"$value\""
                else -> input
            }
        }
        val fishSet = Regex("""^set\s+-g?x\s+([A-Za-z_][A-Za-z0-9_]*)\s+(.+)$""").matchEntire(input)
        if (fishSet != null && source == "fish") {
            val name = fishSet.groupValues[1]
            val value = unquote(fishSet.groupValues[2])
            return when (target) {
                "bash" -> "export $name=\"$value\""
                "powershell" -> "$" + "env:$name = \"$value\""
                else -> input
            }
        }
        val args = input.split(Regex("\\s+"), limit = 2)
        val supported = input == "ls -la" || input == "ls -l" || input == "ls" || input == "pwd" ||
            input == "clear" || input.startsWith("cat ") || input.startsWith("mkdir ") ||
            input.startsWith("rm -rf ") || input.startsWith("git ")
        require(supported) { "هذا الأمر غير مدعوم للتحويل حالياً." }
        return when (args[0]) {
            "ls" -> when {
                input == "ls -la" && target == "powershell" -> "Get-ChildItem -Force"
                input == "ls -l" && target == "powershell" -> "Get-ChildItem"
                target == "powershell" -> "Get-ChildItem"
                else -> input
            }
            "cat" -> if (target == "powershell") "Get-Content ${args.getOrElse(1) { "" }}" else input
            "pwd" -> if (target == "powershell") "Get-Location" else input
            "clear" -> if (target == "powershell") "Clear-Host" else input
            "mkdir" -> if (target == "powershell") "New-Item -ItemType Directory -Name ${args.getOrElse(1) { "" }}" else input
            "rm" -> if (target == "powershell") "Remove-Item -Recurse -Force ${args.getOrElse(1) { "" }}" else input
            "git" -> input
            else -> input
        }
    }
    private fun unquote(value: String): String = value.trim().removeSurrounding("\"").removeSurrounding("'")
}
}env:([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*)$""").matchEntire(input)
        if (assignment != null) {
            val name = assignment.groupValues[1]
            val value = unquote(assignment.groupValues[2])
            return when (target) {
                "bash" -> "export $name=\"$value\""
                "fish" -> "set -gx $name \"$value\""
                else -> input
            }
        }
        val fishSet = Regex("""^set\s+-g?x\s+([A-Za-z_][A-Za-z0-9_]*)\s+(.+)$""").matchEntire(input)
        if (fishSet != null && source == "fish") {
            val name = fishSet.groupValues[1]
            val value = unquote(fishSet.groupValues[2])
            return when (target) {
                "bash" -> "export $name=\"$value\""
                "powershell" -> "$" + "env:$name = \"$value\""
                else -> input
            }
        }
        val args = input.split(Regex("\\s+"), limit = 2)
        val supported = input == "ls -la" || input == "ls -l" || input == "ls" || input == "pwd" ||
            input == "clear" || input.startsWith("cat ") || input.startsWith("mkdir ") ||
            input.startsWith("rm -rf ") || input.startsWith("git ")
        require(supported) { "هذا الأمر غير مدعوم للتحويل حالياً." }
        return when (args[0]) {
            "ls" -> when {
                input == "ls -la" && target == "powershell" -> "Get-ChildItem -Force"
                input == "ls -l" && target == "powershell" -> "Get-ChildItem"
                target == "powershell" -> "Get-ChildItem"
                else -> input
            }
            "cat" -> if (target == "powershell") "Get-Content ${args.getOrElse(1) { "" }}" else input
            "pwd" -> if (target == "powershell") "Get-Location" else input
            "clear" -> if (target == "powershell") "Clear-Host" else input
            "mkdir" -> if (target == "powershell") "New-Item -ItemType Directory -Name ${args.getOrElse(1) { "" }}" else input
            "rm" -> if (target == "powershell") "Remove-Item -Recurse -Force ${args.getOrElse(1) { "" }}" else input
            "git" -> input
            else -> input
        }
    }
    private fun unquote(value: String): String = value.trim().removeSurrounding("\"").removeSurrounding("'")
}
}env:([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*)$""").matchEntire(input)
        if (assignment != null) {
            val name = assignment.groupValues[1]
            val value = unquote(assignment.groupValues[2])
            return when (target) {
                "bash" -> "export $name=\"$value\""
                "fish" -> "set -gx $name \"$value\""
                else -> input
            }
        }
        val fishSet = Regex("""^set\s+-g?x\s+([A-Za-z_][A-Za-z0-9_]*)\s+(.+)$""").matchEntire(input)
        if (fishSet != null && source == "fish") {
            val name = fishSet.groupValues[1]
            val value = unquote(fishSet.groupValues[2])
            return when (target) {
                "bash" -> "export $name=\"$value\""
                "powershell" -> "$" + "env:$name = \"$value\""
                else -> input
            }
        }
        val args = input.split(Regex("\\s+"), limit = 2)
        val supported = input == "ls -la" || input == "ls -l" || input == "ls" || input == "pwd" ||
            input == "clear" || input.startsWith("cat ") || input.startsWith("mkdir ") ||
            input.startsWith("rm -rf ") || input.startsWith("git ")
        require(supported) { "هذا الأمر غير مدعوم للتحويل حالياً." }
        return when (args[0]) {
            "ls" -> when {
                input == "ls -la" && target == "powershell" -> "Get-ChildItem -Force"
                input == "ls -l" && target == "powershell" -> "Get-ChildItem"
                target == "powershell" -> "Get-ChildItem"
                else -> input
            }
            "cat" -> if (target == "powershell") "Get-Content ${args.getOrElse(1) { "" }}" else input
            "pwd" -> if (target == "powershell") "Get-Location" else input
            "clear" -> if (target == "powershell") "Clear-Host" else input
            "mkdir" -> if (target == "powershell") "New-Item -ItemType Directory -Name ${args.getOrElse(1) { "" }}" else input
            "rm" -> if (target == "powershell") "Remove-Item -Recurse -Force ${args.getOrElse(1) { "" }}" else input
            "git" -> input
            else -> input
        }
    }
    private fun unquote(value: String): String = value.trim().removeSurrounding("\"").removeSurrounding("'")
}
