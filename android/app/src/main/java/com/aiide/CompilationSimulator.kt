package com.aiide

import android.content.Context
import org.json.JSONObject

class CompilationSimulator(private val context: Context) {

    fun simulate(sourceCode: String): SimulationResult {
        val issues = mutableListOf<String>()

        val braceCount = sourceCode.count { it == '{' } - sourceCode.count { it == '}' }
        if (braceCount != 0) {
            issues.add("Brace mismatch: $braceCount")
        }

        val parenCount = sourceCode.count { it == '(' } - sourceCode.count { it == ')' }
        if (parenCount != 0) {
            issues.add("Parenthesis mismatch: $parenCount")
        }

        return SimulationResult(issues.isEmpty(), issues)
    }

    data class SimulationResult(val success: Boolean, val issues: List<String>)
}
