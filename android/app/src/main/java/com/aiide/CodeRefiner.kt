package com.aiide

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class CodeRefiner(private val context: Context) {

    companion object {
        private const val TAG = "CodeRefiner"
    }

    data class RefinementRule(
        val pattern: String,
        val replacement: String,
        val description: String
    )

    private val rules = mutableListOf<RefinementRule>()

    init {
        registerDefaultRules()
    }

    private fun registerDefaultRules() {
        rules.add(RefinementRule(
            pattern = "\\.\\.\\.",
            replacement = "...",
            description = "Fix ellipsis"
        ))

        rules.add(RefinementRule(
            pattern = "\\s+",
            replacement = " ",
            description = "Normalize spaces"
        ))

        Log.i(TAG, "Registered ${rules.size} refinement rules")
    }

    fun refine(code: String): String {
        var refined = code
        for (rule in rules) {
            refined = refined.replace(Regex(rule.pattern), rule.replacement)
        }
        return refined
    }

    fun addRule(pattern: String, replacement: String, description: String) {
        rules.add(RefinementRule(pattern, replacement, description))
    }

    fun getRules(): List<RefinementRule> = rules.toList()
}
