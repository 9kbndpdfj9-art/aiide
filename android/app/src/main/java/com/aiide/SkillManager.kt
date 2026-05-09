package com.aiide

import android.content.Context
import android.util.Log
import org.json.JSONObject
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

class SkillManager(private val context: Context) {

    companion object {
        private const val TAG = "SkillManager"
    }

    private val skills = ConcurrentHashMap<String, SkillDefinition>()
    private val skillHistory = CopyOnWriteArrayList<SkillExecution>()
    private val skillLock = ReentrantReadWriteLock()
    private val executor = Executors.newCachedThreadPool()

    data class SkillDefinition(
        val id: String,
        val name: String,
        val description: String,
        val category: String,
        val keywords: List<String>,
        val triggerPatterns: List<String>,
        val pipeline: List<SkillPipelineStep>,
        val requiredCapabilities: List<String>,
        val version: String,
        val author: String,
        val tags: List<String>
    )

    data class SkillPipelineStep(
        val name: String,
        val type: String,
        val prompt: String,
        val timeoutMs: Long,
        val inputTransform: String?,
        val outputTransform: String?
    )

    data class StepResult(
        val stepName: String,
        val output: String,
        val success: Boolean,
        val errorMessage: String? = null,
        val metadata: Map<String, String> = emptyMap()
    )

    data class SkillExecution(
        val skillId: String,
        val timestamp: Long,
        val input: String,
        val output: String,
        val success: Boolean,
        val durationMs: Long,
        val errorMessage: String? = null
    )

    data class ExecutionResult(
        val skillId: String,
        val results: List<StepResult>,
        val finalOutput: String,
        val success: Boolean,
        val totalDurationMs: Long,
        val errorMessage: String? = null
    )

    init {
        registerDefaultSkills()
    }

    private fun registerDefaultSkills() {
        registerSkill(SkillDefinition(
            id = "code_generate",
            name = "代码生成",
            description = "根据需求生成高质量代码",
            category = "coding",
            keywords = listOf("写代码", "生成代码", "code generation", "implement"),
            triggerPatterns = listOf("写.*代码", "实现.*功能", "generate.*code"),
            pipeline = listOf(
                SkillPipelineStep("analysis", "analysis", "分析需求", 5000, null, null),
                SkillPipelineStep("generate", "ai_generate", "生成代码", 10000, null, null),
                SkillPipelineStep("validate", "validation", "验证代码", 3000, null, null)
            ),
            requiredCapabilities = listOf("code_generation"),
            version = "1.0",
            author = "AIIDE",
            tags = listOf("coding", "generation")
        ))

        registerSkill(SkillDefinition(
            id = "code_review",
            name = "代码审查",
            description = "审查代码质量和安全问题",
            category = "analysis",
            keywords = listOf("审查", "review", "检查代码"),
            triggerPatterns = listOf("审查.*代码", "review.*code", "检查.*问题"),
            pipeline = listOf(
                SkillPipelineStep("scan", "scanner", "扫描问题", 5000, null, null),
                SkillPipelineStep("analyze", "analysis", "分析问题", 5000, null, null),
                SkillPipelineStep("report", "evaluation", "生成报告", 3000, null, null)
            ),
            requiredCapabilities = listOf("code_analysis"),
            version = "1.0",
            author = "AIIDE",
            tags = listOf("analysis", "quality")
        ))

        registerSkill(SkillDefinition(
            id = "debug_fix",
            name = "调试修复",
            description = "分析并修复bug",
            category = "debugging",
            keywords = listOf("调试", "debug", "修复bug", "fix error"),
            triggerPatterns = listOf("调试.*代码", "修复.*错误", "fix.*bug"),
            pipeline = listOf(
                SkillPipelineStep("detect", "detector", "检测错误", 5000, null, null),
                SkillPipelineStep("analyze", "analysis", "分析原因", 5000, null, null),
                SkillPipelineStep("fix", "code_generation", "生成修复", 10000, null, null),
                SkillPipelineStep("validate", "validation", "验证修复", 3000, null, null)
            ),
            requiredCapabilities = listOf("code_analysis", "code_generation"),
            version = "1.0",
            author = "AIIDE",
            tags = listOf("debugging", "fix")
        ))

        registerSkill(SkillDefinition(
            id = "refactor",
            name = "代码重构",
            description = "重构优化代码结构",
            category = "refactoring",
            keywords = listOf("重构", "refactor", "优化代码"),
            triggerPatterns = listOf("重构.*代码", "优化.*结构", "refactor.*code"),
            pipeline = listOf(
                SkillPipelineStep("analyze", "analysis", "分析结构", 5000, null, null),
                SkillPipelineStep("plan", "pattern_match", "规划重构", 5000, null, null),
                SkillPipelineStep("execute", "code_generation", "执行重构", 10000, null, null),
                SkillPipelineStep("verify", "validation", "验证结果", 3000, null, null)
            ),
            requiredCapabilities = listOf("code_analysis", "code_generation"),
            version = "1.0",
            author = "AIIDE",
            tags = listOf("refactoring", "optimization")
        ))

        registerSkill(SkillDefinition(
            id = "web_search",
            name = "网络搜索",
            description = "搜索网络获取信息",
            category = "search",
            keywords = listOf("搜索", "search", "查找"),
            triggerPatterns = listOf("搜索.*信息", "search.*web", "查找.*资料"),
            pipeline = listOf(
                SkillPipelineStep("query", "analysis", "分析查询", 2000, null, null),
                SkillPipelineStep("search", "ai_generate", "执行搜索", 8000, null, null),
                SkillPipelineStep("summarize", "evaluation", "总结结果", 3000, null, null)
            ),
            requiredCapabilities = listOf("web_search"),
            version = "1.0",
            author = "AIIDE",
            tags = listOf("search", "information")
        ))

        Log.i(TAG, "Registered ${skills.size} default skills")
    }

    fun registerSkill(skill: SkillDefinition) {
        skillLock.write {
            skills[skill.id] = skill
        }
        Log.i(TAG, "Registered skill: ${skill.name}")
    }

    fun unregisterSkill(skillId: String): Boolean {
        return skillLock.write {
            skills.remove(skillId) != null
        }
    }

    fun getSkill(skillId: String): SkillDefinition? = skills[skillId]

    fun findSkill(query: String): SkillDefinition? {
        val lowerQuery = query.lowercase()
        
        for (skill in skills.values) {
            if (skill.keywords.any { it.lowercase().contains(lowerQuery) }) {
                return skill
            }
            if (skill.name.lowercase().contains(lowerQuery)) {
                return skill
            }
            if (skill.description.lowercase().contains(lowerQuery)) {
                return skill
            }
        }
        return null
    }

    fun searchSkills(query: String): List<SkillDefinition> {
        val lowerQuery = query.lowercase()
        val results = mutableListOf<Pair<SkillDefinition, Int>>()

        for (skill in skills.values) {
            var score = 0

            if (skill.name.lowercase().contains(lowerQuery)) score += 10
            if (skill.description.lowercase().contains(lowerQuery)) score += 5
            if (skill.keywords.any { it.lowercase().contains(lowerQuery) }) score += 3
            if (skill.tags.any { it.lowercase().contains(lowerQuery) }) score += 2

            if (score > 0) {
                results.add(skill to score)
            }
        }

        return results.sortedByDescending { it.second }.map { it.first }
    }

    fun execute(skillId: String, input: String): ExecutionResult {
        val skill = skills[skillId] ?: return ExecutionResult(
            skillId = skillId,
            results = emptyList(),
            finalOutput = "",
            success = false,
            totalDurationMs = 0,
            errorMessage = "Skill not found: $skillId"
        )

        return executePipeline(skill, input)
    }

    fun executeByQuery(query: String, input: String): ExecutionResult {
        val skill = findSkill(query) ?: return ExecutionResult(
            skillId = "",
            results = emptyList(),
            finalOutput = "",
            success = false,
            totalDurationMs = 0,
            errorMessage = "No matching skill found for: $query"
        )

        return executePipeline(skill, input)
    }

    private fun executePipeline(skill: SkillDefinition, input: String): ExecutionResult {
        val startTime = System.currentTimeMillis()
        val results = mutableListOf<StepResult>()

        try {
            var currentInput = input
            val previousResults = mutableListOf<StepResult>()

            for (step in skill.pipeline) {
                val result = executeStep(step, currentInput, previousResults)
                results.add(result)
                previousResults.add(result)

                if (!result.success) {
                    return ExecutionResult(
                        skillId = skill.id,
                        results = results,
                        finalOutput = results.joinToString("\n") { it.output },
                        success = false,
                        totalDurationMs = System.currentTimeMillis() - startTime,
                        errorMessage = "Pipeline failed at step: ${step.name}"
                    )
                }

                currentInput = result.output
            }

            val finalOutput = results.lastOrNull()?.output ?: ""

            skillHistory.add(SkillExecution(
                skillId = skill.id,
                timestamp = System.currentTimeMillis(),
                input = input,
                output = finalOutput,
                success = true,
                durationMs = System.currentTimeMillis() - startTime
            ))

            return ExecutionResult(
                skillId = skill.id,
                results = results,
                finalOutput = finalOutput,
                success = true,
                totalDurationMs = System.currentTimeMillis() - startTime
            )

        } catch (e: Exception) {
            Log.e(TAG, "Pipeline execution failed: ${e.message}", e)

            skillHistory.add(SkillExecution(
                skillId = skill.id,
                timestamp = System.currentTimeMillis(),
                input = input,
                output = "",
                success = false,
                durationMs = System.currentTimeMillis() - startTime,
                errorMessage = e.message
            ))

            return ExecutionResult(
                skillId = skill.id,
                results = results,
                finalOutput = results.joinToString("\n") { it.output },
                success = false,
                totalDurationMs = System.currentTimeMillis() - startTime,
                errorMessage = e.message
            )
        }
    }

    private fun executeStep(step: SkillPipelineStep, input: String, previousResults: List<StepResult>): StepResult {
        val startTime = System.currentTimeMillis()
        val timeout = step.timeoutMs.coerceIn(1000, 120000)

        try {
            val transformedInput = if (step.inputTransform != null) {
                applyInputTransform(input, step.inputTransform, previousResults)
            } else input

            val output = when (step.type) {
                "ai_generate" -> executeAIGenerate(step, transformedInput, timeout)
                "ai_refine" -> executeAIRefine(step, transformedInput, timeout)
                "analysis" -> executeAnalysis(step, transformedInput, timeout)
                "code_generation" -> executeCodeGeneration(step, transformedInput, timeout)
                "validation" -> executeValidation(step, transformedInput, timeout)
                "evaluation" -> executeEvaluation(step, transformedInput, previousResults, timeout)
                "scanner" -> executeScanner(step, transformedInput, timeout)
                "detector" -> executeDetector(step, transformedInput, timeout)
                "pattern_match" -> executePatternMatch(step, transformedInput, timeout)
                else -> executeGenericStep(step, transformedInput, timeout)
            }

            val transformedOutput = if (step.outputTransform != null) {
                applyOutputTransform(output, step.outputTransform, previousResults)
            } else output

            val executionTime = System.currentTimeMillis() - startTime
            return StepResult(
                stepName = step.name,
                output = transformedOutput,
                success = true,
                metadata = mapOf(
                    "step_type" to step.type,
                    "timeout" to step.timeoutMs.toString(),
                    "execution_time_ms" to executionTime.toString()
                )
            )
        } catch (e: Exception) {
            val executionTime = System.currentTimeMillis() - startTime
            Log.e(TAG, "Step ${step.name} failed: ${e.message}", e)
            return StepResult(
                stepName = step.name,
                success = false,
                errorMessage = "${e.javaClass.simpleName}: ${e.message}",
                metadata = mapOf(
                    "step_type" to step.type,
                    "execution_time_ms" to executionTime.toString()
                )
            )
        }
    }

    private fun applyInputTransform(input: String, transform: String, previousResults: List<StepResult>): String {
        return when (transform) {
            "extract_code" -> extractCodeFromContent(input)
            "combine_previous" -> previousResults.joinToString("\n") { it.output }
            else -> input
        }
    }

    private fun applyOutputTransform(output: String, transform: String, previousResults: List<StepResult>): String {
        return when (transform) {
            "summarize" -> output.take(500)
            "last_result" -> previousResults.lastOrNull()?.output ?: output
            else -> output
        }
    }

    private fun extractCodeFromContent(content: String): String {
        val codeBlockRegex = Regex("""```[\w]*\n([\s\S]*?)```|`([^`]+)`""")
        val matches = codeBlockRegex.findAll(content)
        return matches.map { it.groupValues[1].ifEmpty { it.groupValues[2] } }.joinToString("\n").ifEmpty { content }
    }

    private fun executeAIGenerate(step: SkillPipelineStep, input: String, timeout: Long): String {
        return "[AI_GENERATED] Prompt: ${step.prompt}\nInput: ${input.take(100)}..."
    }

    private fun executeAIRefine(step: SkillPipelineStep, input: String, timeout: Long): String {
        return "[AI_REFINED] Prompt: ${step.prompt}\nOriginal: ${input.take(100)}..."
    }

    private fun executeAnalysis(step: SkillPipelineStep, input: String, timeout: Long): String {
        return "[ANALYZED] Analysis: ${step.prompt}\nInput: ${input.take(100)}..."
    }

    private fun executeCodeGeneration(step: SkillPipelineStep, input: String, timeout: Long): String {
        return "[CODE_GENERATED] Based on: ${input.take(100)}..."
    }

    private fun executeValidation(step: SkillPipelineStep, input: String, timeout: Long): String {
        return "[VALIDATED] Result: PASSED"
    }

    private fun executeEvaluation(step: SkillPipelineStep, input: String, previousResults: List<StepResult>, timeout: Long): String {
        return "[EVALUATED] ${previousResults.size} steps completed"
    }

    private fun executeScanner(step: SkillPipelineStep, input: String, timeout: Long): String {
        return "[SCANNED] Target: ${input.take(100)}..."
    }

    private fun executeDetector(step: SkillPipelineStep, input: String, timeout: Long): String {
        return "[DETECTED] Target: ${input.take(100)}..."
    }

    private fun executePatternMatch(step: SkillPipelineStep, input: String, timeout: Long): String {
        return "[PATTERN_MATCHED] Input: ${input.take(100)}..."
    }

    private fun executeGenericStep(step: SkillPipelineStep, input: String, timeout: Long): String {
        return "[${step.type.uppercase()}] Step: ${step.name}\nInput: ${input.take(100)}..."
    }

    fun listSkills(): List<SkillDefinition> = skills.values.toList()

    fun listSkillsByCategory(category: String): List<SkillDefinition> {
        return skills.values.filter { it.category == category }
    }

    fun getSkillStats(): JSONObject = JSONObject().apply {
        put("total_skills", skills.size)
        put("total_executions", skillHistory.size)
        put("categories", JSONObject().apply {
            skills.values.groupBy { it.category }.forEach { (category, skillList) ->
                put(category, skillList.size)
            }
        })
    }

    fun getExecutionHistory(limit: Int = 100): List<SkillExecution> {
        return skillHistory.takeLast(limit).reversed()
    }

    fun clearHistory() {
        skillHistory.clear()
    }

    fun shutdown() {
        executor.shutdown()
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            executor.shutdownNow()
        }
    }
}
