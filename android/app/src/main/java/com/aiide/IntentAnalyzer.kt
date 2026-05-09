package com.aiide

import android.content.Context
import org.json.JSONObject

class IntentAnalyzer(private val context: Context) {

    fun analyze(input: String): IntentResult {
        val intent = detectIntent(input)
        val entities = extractEntities(input)
        val sentiment = analyzeSentiment(input)

        return IntentResult(intent, entities, sentiment)
    }

    private fun detectIntent(input: String): String {
        return when {
            input.contains("生成") || input.contains("创建") -> "code_generate"
            input.contains("调试") || input.contains("错误") -> "debug"
            input.contains("搜索") || input.contains("查找") -> "search"
            input.contains("审查") || input.contains("检查") -> "review"
            input.contains("重构") || input.contains("优化") -> "refactor"
            else -> "general"
        }
    }

    private fun extractEntities(input: String): List<String> {
        val entityRegex = Regex("""[A-Z][a-z]+\\[w]+""")
        return entityRegex.findAll(input).map { it.value }.toList()
    }

    private fun analyzeSentiment(input: String): String {
        return when {
            input.contains("好") || input.contains("棒") -> "positive"
            input.contains("错误") || input.contains("问题") -> "negative"
            else -> "neutral"
        }
    }

    data class IntentResult(
        val intent: String,
        val entities: List<String>,
        val sentiment: String
    )
}
