package com.aiide

import android.content.Context
import org.json.JSONObject

class CodeQualityGate(private val context: Context) {

    data class QualityResult(
        val passed: Boolean,
        val score: Double,
        val issues: List<String>
    )

    fun check(code: String): QualityResult {
        val issues = mutableListOf<String>()
        var score = 100.0

        if (code.contains("TODO") || code.contains("FIXME")) {
            issues.add("Contains TODO/FIXME comments")
            score -= 5
        }

        if (code.length > 1000) {
            issues.add("Method too long (>1000 lines)")
            score -= 10
        }

        if (!code.contains("//") && !code.contains("/*")) {
            issues.add("No comments found")
            score -= 5
        }

        return QualityResult(score >= 80, score, issues)
    }
}
