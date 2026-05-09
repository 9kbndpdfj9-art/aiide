package com.aiide

import android.content.Context
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class SemanticSkillMatcher(private val context: Context) {

    private val skills = ConcurrentHashMap<String, SkillInfo>()
    private val matchHistory = mutableListOf<MatchResult>()

    data class SkillInfo(
        val id: String,
        val name: String,
        val keywords: List<String>,
        val description: String,
        val score: Double
    )

    data class MatchResult(
        val query: String,
        val matchedSkill: String,
        val confidence: Double,
        val timestamp: Long
    )

    init {
        registerDefaultSkills()
    }

    private fun registerDefaultSkills() {
        skills["code_generate"] = SkillInfo(
            id = "code_generate",
            name = "代码生成",
            keywords = listOf("写代码", "生成代码", "实现", "create code"),
            description = "Generate code from requirements",
            score = 0.0
        )

        skills["debug"] = SkillInfo(
            id = "debug",
            name = "调试修复",
            keywords = listOf("调试", "修复bug", "fix error"),
            description = "Debug and fix issues",
            score = 0.0
        )

        skills["search"] = SkillInfo(
            id = "search",
            name = "网络搜索",
            keywords = listOf("搜索", "查找", "search"),
            description = "Search the web",
            score = 0.0
        )
    }

    fun match(query: String): SkillInfo? {
        val lowerQuery = query.lowercase()
        var bestMatch: SkillInfo? = null
        var bestScore = 0.0

        for (skill in skills.values) {
            val score = calculateMatchScore(lowerQuery, skill)
            if (score > bestScore) {
                bestScore = score
                bestMatch = skill
            }
        }

        if (bestMatch != null) {
            matchHistory.add(MatchResult(
                query = query,
                matchedSkill = bestMatch.id,
                confidence = bestScore,
                timestamp = System.currentTimeMillis()
            ))
        }

        return bestMatch
    }

    private fun calculateMatchScore(query: String, skill: SkillInfo): Double {
        var score = 0.0

        for (keyword in skill.keywords) {
            if (query.contains(keyword.lowercase())) {
                score += 0.3
            }
        }

        if (query.contains(skill.name.lowercase())) {
            score += 0.5
        }

        return score.coerceIn(0.0, 1.0)
    }

    fun toJson(result: MatchResult): String = JSONObject().apply {
        put("query", result.query)
        put("matchedSkill", result.matchedSkill)
        put("confidence", result.confidence)
        put("timestamp", result.timestamp)
    }.toString()
}
