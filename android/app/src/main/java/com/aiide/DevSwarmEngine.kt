package com.aiide

import android.content.Context
import org.json.JSONObject
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

class DevSwarmEngine(private val modelRouter: ModelRouter) {

    private val agents = ConcurrentHashMap<String, SwarmAgent>()
    private val agentLock = ReentrantReadWriteLock()
    private val executor = Executors.newCachedThreadPool()

    data class SwarmAgent(
        val id: String,
        val name: String,
        val role: String,
        val model: String,
        val capabilities: List<String>,
        val status: AgentStatus,
        val currentTask: String?,
        val completedTasks: Int,
        val failedTasks: Int
    )

    enum class AgentStatus { IDLE, WORKING, BLOCKED, TERMINATED }

    data class Task(
        val id: String,
        val description: String,
        val priority: Int,
        val assignedAgent: String?,
        val status: TaskStatus,
        val result: String?,
        val dependencies: List<String>
    )

    enum class TaskStatus { PENDING, IN_PROGRESS, COMPLETED, FAILED, BLOCKED }

    init {
        initializeDefaultAgents()
    }

    private fun initializeDefaultAgents() {
        createAgent("agent_planner", "规划Agent", "planner", listOf("planning", "decomposition", "coordination"))
        createAgent("agent_coder", "编码Agent", "coder", listOf("code_generation", "refactoring", "debugging"))
        createAgent("agent_reviewer", "审查Agent", "reviewer", listOf("code_review", "quality_check", "security_scan"))
        createAgent("agent_tester", "测试Agent", "tester", listOf("test_generation", "test_execution", "coverage_analysis"))
        createAgent("agent_docs", "文档Agent", "documenter", listOf("documentation", "api_docs", "readme"))
    }

    fun createAgent(id: String, name: String, role: String, capabilities: List<String>): Boolean {
        return agentLock.write {
            if (agents.containsKey(id)) return@write false
            val model = modelRouter.route("agent task", mapOf("role" to role))
            agents[id] = SwarmAgent(
                id = id,
                name = name,
                role = role,
                model = model,
                capabilities = capabilities,
                status = AgentStatus.IDLE,
                currentTask = null,
                completedTasks = 0,
                failedTasks = 0
            )
            true
        }
    }

    fun getAgent(agentId: String): SwarmAgent? = agents[agentId]

    fun listAgents(): List<SwarmAgent> = agents.values.toList()

    fun assignTask(agentId: String, task: String): Boolean {
        agentLock.write {
            val agent = agents[agentId] ?: return@write false
            agents[agentId] = agent.copy(
                status = AgentStatus.WORKING,
                currentTask = task
            )
        }
        executor.execute {
            processTask(agentId, task)
        }
        return true
    }

    private fun processTask(agentId: String, task: String) {
        try {
            Thread.sleep(1000)
            agentLock.write {
                val agent = agents[agentId] ?: return
                agents[agentId] = agent.copy(
                    status = AgentStatus.IDLE,
                    currentTask = null,
                    completedTasks = agent.completedTasks + 1
                )
            }
        } catch (e: Exception) {
            agentLock.write {
                val agent = agents[agentId] ?: return
                agents[agentId] = agent.copy(
                    status = AgentStatus.IDLE,
                    failedTasks = agent.failedTasks + 1
                )
            }
        }
    }

    fun coordinate(task: String): String {
        val subtasks = decomposeTask(task)
        val results = mutableListOf<String>()

        for (subtask in subtasks) {
            val agent = findBestAgent(subtask)
            if (agent != null) {
                assignTask(agent.id, subtask)
                results.add("Assigned: ${subtask} to ${agent.name}")
            }
        }

        return JSONObject().apply {
            put("task", task)
            put("subtasks", subtasks.size)
            put("assignments", results)
        }.toString()
    }

    private fun decomposeTask(task: String): List<String> {
        return listOf(
            "分析需求: $task",
            "设计架构",
            "实现代码",
            "编写测试",
            "审查代码",
            "生成文档"
        )
    }

    private fun findBestAgent(task: String): SwarmAgent? {
        return agents.values
            .filter { it.status == AgentStatus.IDLE }
            .firstOrNull()
    }

    fun getSwarmStats(): JSONObject = JSONObject().apply {
        put("total_agents", agents.size)
        put("idle_agents", agents.values.count { it.status == AgentStatus.IDLE })
        put("working_agents", agents.values.count { it.status == AgentStatus.WORKING })
        put("total_completed", agents.values.sumOf { it.completedTasks })
        put("total_failed", agents.values.sumOf { it.failedTasks })
    }

    fun shutdown() {
        executor.shutdown()
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            executor.shutdownNow()
        }
    }
}
