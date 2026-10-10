package com.devtools.utils.tools

 data class WordStats(val words: Int, val characters: Int, val lines: Int, val paragraphs: Int)

class WordCounterTool {
    fun execute(input: String): WordStats {
        val words = WORD_PATTERN.findAll(input).count()
        val characters = input.codePointCount(0, input.length)
        val lines = if (input.isEmpty()) 0 else input.split('\n').size
        val paragraphs = if (input.isBlank()) 0 else input.trim()
            .split(Regex("\\n[\\t \\r]*\\n+"))
            .count { it.isNotBlank() }
        return WordStats(words, characters, lines, paragraphs)
    }

    private companion object {
        val WORD_PATTERN = Regex("[\\p{L}\\p{N}_]+(?:['’][\\p{L}\\p{N}_]+)*")
    }
}
