package com.aiide

import android.content.Context
import org.json.JSONObject

class CodeReviewEngine(private val context: Context) {

    data class ReviewResult(
        val file: String,
        val issues: List<Issue>,
        val score: Double,
        val suggestions: List<String>
    )

    data class Issue(
        val severity: Severity,
        val type: IssueType,
        val message: String,
        val line: Int
    )

    enum class Severity { ERROR, WARNING, INFO }
    enum class IssueType { STYLE, PERFORMANCE, SECURITY, BUG, BEST_PRACTICE }

    fun review(code: String, file: String): ReviewResult {
        val issues = mutableListOf<Issue>()
        val suggestions = mutableListOf<String>()

        if (code.contains("println")) {
            issues.add(Issue(Severity.WARNING, IssueType.BEST_PRACTICE, "Avoid using println, use Log instead", 0))
            suggestions.add("Replace println with android.util.Log")
        }

        if (Regex("\\.\\.\\.").containsMatchIn(code)) {
            issues.add(Issue(Severity.WARNING, IssueType.STYLE, "Use ... for varargs", 0))
        }

        if (code.contains("Thread.sleep")) {
            issues.add(Issue(Severity.INFO, IssueType.PERFORMANCE, "Consider using Handler or Coroutines", 0))
        }

        val score = computeScore(issues)

        return ReviewResult(file, issues, score, suggestions)
    }

    private fun computeScore(issues: List<Issue>): Double {
        val errorCount = issues.count { it.severity == Severity.ERROR } * 10
        val warningCount = issues.count { it.severity == Severity.WARNING } * 3
        val infoCount = issues.count { it.severity == Severity.INFO }

        return (100.0 - errorCount - warningCount - infoCount).coerceIn(0.0, 100.0)
    }
}
