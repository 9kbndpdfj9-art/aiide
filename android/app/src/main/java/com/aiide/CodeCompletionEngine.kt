package com.aiide

import android.content.Context
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class CodeCompletionEngine {

    private val completions = ConcurrentHashMap<String, List<Completion>>()

    data class Completion(
        val text: String,
        val kind: String,
        val score: Double
    )

    fun getCompletions(prefix: String, context: String): List<Completion> {
        val commonCompletions = listOf(
            Completion("fun ", "keyword", 0.9),
            Completion("val ", "keyword", 0.9),
            Completion("var ", "keyword", 0.9),
            Completion("class ", "keyword", 0.9),
            Completion("interface ", "keyword", 0.8),
            Completion("if ", "keyword", 0.8),
            Completion("else ", "keyword", 0.8),
            Completion("for ", "keyword", 0.8),
            Completion("while ", "keyword", 0.8),
            Completion("return ", "keyword", 0.8),
            Completion("try ", "keyword", 0.7),
            Completion("catch ", "keyword", 0.7),
            Completion("throw ", "keyword", 0.7),
            Completion("import ", "keyword", 0.7),
            Completion("package ", "keyword", 0.7)
        )

        return commonCompletions.filter { it.text.startsWith(prefix) }
    }

    fun inlineComplete(code: String, position: Int): String {
        val before = code.substring(0, position)
        val after = code.substring(position)

        val lastWord = Regex("\\w+$").find(before)?.value ?: ""
        val completions = getCompletions(lastWord, before)

        if (completions.isEmpty()) return code

        val completion = completions.first()
        return before + completion.text + after
    }
}
