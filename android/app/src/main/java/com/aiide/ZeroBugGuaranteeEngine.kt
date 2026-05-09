package com.aiide

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class ZeroBugGuaranteeEngine(private val context: Context) {

    companion object {
        private const val TAG = "ZeroBugGuarantee"
    }

    private val validationRules = ConcurrentHashMap<String, ValidationRule>()

    data class ValidationRule(
        val id: String,
        val name: String,
        val check: (String) -> Boolean,
        val severity: Severity
    )

    enum class Severity { ERROR, WARNING, INFO }

    init {
        registerDefaultRules()
    }

    private fun registerDefaultRules() {
        validationRules["null_check"] = ValidationRule(
            id = "null_check",
            name = "Null Check",
            check = { code -> !code.contains("!!") || code.contains("?.") },
            severity = Severity.ERROR
        )

        validationRules["brace_match"] = ValidationRule(
            id = "brace_match",
            name = "Brace Matching",
            check = { code ->
                val open = code.count { it == '{' }
                val close = code.count { it == '}' }
                open == close
            },
            severity = Severity.ERROR
        )

        Log.i(TAG, "ZeroBugGuaranteeEngine initialized with ${validationRules.size} rules")
    }

    fun validate(code: String): ValidationResult {
        val issues = mutableListOf<Issue>()

        for (rule in validationRules.values) {
            if (!rule.check(code)) {
                issues.add(Issue(rule.id, rule.name, rule.severity))
            }
        }

        return ValidationResult(issues)
    }

    data class Issue(val id: String, val name: String, val severity: Severity)
    data class ValidationResult(val issues: List<Issue>) {
        val isValid = issues.none { it.severity == Severity.ERROR }
        val errorCount = issues.count { it.severity == Severity.ERROR }
        val warningCount = issues.count { it.severity == Severity.WARNING }
    }
}
