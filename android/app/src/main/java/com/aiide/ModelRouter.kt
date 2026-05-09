package com.aiide

import android.content.Context
import org.json.JSONObject
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

class ModelRouter(private val context: Context) {

    private val modelConfigs = ConcurrentHashMap<String, ModelConfig>()
    private val loadBalancer = RoundRobinLoadBalancer()
    private val configLock = ReentrantReadWriteLock()

    data class ModelConfig(
        val name: String,
        val provider: String,
        val apiKey: String,
        val baseUrl: String,
        val maxTokens: Int,
        val temperature: Float,
        val priority: Int,
        val capabilities: List<String>,
        val costPerToken: Double
    )

    init {
        initializeDefaultModels()
    }

    private fun initializeDefaultModels() {
        val models = JSONArray().apply {
            put(JSONObject().apply {
                put("name", "claude-3-opus")
                put("provider", "anthropic")
                put("capabilities", JSONArray().apply {
                    put("code_generation")
                    put("code_analysis")
                    put("reasoning")
                    put("multimodal")
                })
                put("priority", 1)
                put("max_tokens", 200000)
                put("temperature", 0.7f)
            })
            put(JSONObject().apply {
                put("name", "claude-3-sonnet")
                put("provider", "anthropic")
                put("capabilities", JSONArray().apply {
                    put("code_generation")
                    put("code_analysis")
                    put("fast")
                })
                put("priority", 2)
                put("max_tokens", 200000)
                put("temperature", 0.5f)
            })
            put(JSONObject().apply {
                put("name", "gpt-4-turbo")
                put("provider", "openai")
                put("capabilities", JSONArray().apply {
                    put("code_generation")
                    put("function_calling")
                    put("vision")
                })
                put("priority", 3)
                put("max_tokens", 128000)
                put("temperature", 0.7f)
            })
            put(JSONObject().apply {
                put("name", "gpt-3.5-turbo")
                put("provider", "openai")
                put("capabilities", JSONArray().apply {
                    put("code_generation")
                    put("fast")
                })
                put("priority", 4)
                put("max_tokens", 16385)
                put("temperature", 0.5f)
            })
        }

        for (i in 0 until models.length()) {
            val model = models.getJSONObject(i)
            val name = model.getString("name")
            val provider = model.getString("provider")
            val capabilitiesArray = model.getJSONArray("capabilities")
            val capabilities = (0 until capabilitiesArray.length()).map { capabilitiesArray.getString(it) }

            modelConfigs[name] = ModelConfig(
                name = name,
                provider = provider,
                apiKey = "",
                baseUrl = getDefaultBaseUrl(provider),
                maxTokens = model.optInt("max_tokens", 8192),
                temperature = model.optDouble("temperature", 0.7).toFloat(),
                priority = model.optInt("priority", 5),
                capabilities = capabilities,
                costPerToken = getCostPerToken(provider)
            )
        }
    }

    private fun getDefaultBaseUrl(provider: String): String = when (provider) {
        "anthropic" -> "https://api.anthropic.com"
        "openai" -> "https://api.openai.com/v1"
        else -> ""
    }

    private fun getCostPerToken(provider: String): Double = when (provider) {
        "anthropic" -> 0.000015
        "openai" -> 0.00001
        else -> 0.00001
    }

    fun route(task: String, context: Map<String, Any>): String {
        val complexity = computeComplexity(task, context)
        val requiredCapabilities = extractCapabilities(task)

        val candidateModels = modelConfigs.values
            .filter { model ->
                requiredCapabilities.all { cap ->
                    model.capabilities.contains(cap) || model.capabilities.contains("general")
                }
            }
            .sortedBy { it.priority }

        if (candidateModels.isEmpty()) {
            return modelConfigs.values.minByOrNull { it.priority }?.name ?: "gpt-3.5-turbo"
        }

        return when {
            complexity > 0.8 && candidateModels.any { it.capabilities.contains("reasoning") } -> {
                candidateModels.firstOrNull { it.capabilities.contains("reasoning") }?.name
                    ?: candidateModels.first().name
            }
            complexity > 0.5 -> candidateModels.first().name
            else -> candidateModels.lastOrNull()?.name ?: "gpt-3.5-turbo"
        }
    }

    private fun computeComplexity(task: String, context: Map<String, Any>): Double {
        var complexity = 0.0

        val complexityKeywords = listOf(
            "分析" to 0.15, "设计" to 0.2, "架构" to 0.25,
            "优化" to 0.2, "重构" to 0.2, "调试" to 0.15,
            "实现" to 0.1, "修复" to 0.1, "审查" to 0.15,
            "生成" to 0.05, "解释" to 0.05, "学习" to 0.1
        )

        for ((keyword, weight) in complexityKeywords) {
            if (task.contains(keyword)) {
                complexity += weight
            }
        }

        val linesOfCode = context["lines_of_code"] as? Int ?: 0
        if (linesOfCode > 1000) complexity += 0.3
        else if (linesOfCode > 500) complexity += 0.2
        else if (linesOfCode > 100) complexity += 0.1

        val fileCount = context["file_count"] as? Int ?: 0
        if (fileCount > 10) complexity += 0.2
        else if (fileCount > 5) complexity += 0.1

        return complexity.coerceIn(0.0, 1.0)
    }

    private fun extractCapabilities(task: String): List<String> {
        val capabilities = mutableListOf<String>()

        if (task.contains("代码") || task.contains("code")) capabilities.add("code_generation")
        if (task.contains("分析") || task.contains("analyze")) capabilities.add("code_analysis")
        if (task.contains("推理") || task.contains("reason")) capabilities.add("reasoning")
        if (task.contains("图片") || task.contains("image")) capabilities.add("vision")
        if (task.contains("快速") || task.contains("fast")) capabilities.add("fast")
        if (task.contains("函数") || task.contains("function")) capabilities.add("function_calling")

        if (capabilities.isEmpty()) capabilities.add("general")

        return capabilities
    }

    fun getModelConfig(modelName: String): ModelConfig? = modelConfigs[modelName]

    fun addModel(config: ModelConfig) {
        configLock.write {
            modelConfigs[config.name] = config
        }
    }

    fun removeModel(modelName: String) {
        configLock.write {
            modelConfigs.remove(modelName)
        }
    }

    fun listModels(): List<String> = modelConfigs.keys.toList()

    fun getModelStats(): JSONObject = JSONObject().apply {
        put("total_models", modelConfigs.size)
        put("models", JSONArray().apply {
            modelConfigs.forEach { (name, config) ->
                put(JSONObject().apply {
                    put("name", name)
                    put("provider", config.provider)
                    put("capabilities", JSONArray(config.capabilities))
                    put("priority", config.priority)
                })
            }
        })
    }

    fun loadBalance(modelNames: List<String>): String {
        return loadBalancer.select(modelNames)
    }

    class RoundRobinLoadBalancer {
        private val counters = ConcurrentHashMap<String, Int>()

        fun select(models: List<String>): String {
            if (models.isEmpty()) return ""
            if (models.size == 1) return models.first()

            val counter = counters.getOrPut("global") { 0 }
            val index = counter % models.size
            counters["global"] = counter + 1
            return models[index]
        }
    }
}
