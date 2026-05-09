package com.aiide

import android.content.Context
import android.util.Log
import org.json.JSONObject

class ThinkingRouterEngine(private val context: Context) {

    companion object {
        private const val TAG = "ThinkingRouter"
    }

    private val routeHistory = mutableListOf<RouteEntry>()
    private val routeConfig = mutableMapOf<String, RouteRule>()

    data class RouteRule(
        val pattern: String,
        val target: String,
        val priority: Int
    )

    data class RouteEntry(
        val query: String,
        val routedTo: String,
        val timestamp: Long
    )

    init {
        initializeDefaultRoutes()
    }

    private fun initializeDefaultRoutes() {
        routeConfig["debug"] = RouteRule("debug|调试|错误|exception", "AutoDebugger", 10)
        routeConfig["generate"] = RouteRule("生成|创建|写代码", "CodeGenerator", 10)
        routeConfig["search"] = RouteRule("搜索|查找|search", "WebSearchEngine", 10)
        routeConfig["refactor"] = RouteRule("重构|优化|refactor", "RefactorEngine", 10)

        Log.i(TAG, "Initialized with ${routeConfig.size} route rules")
    }

    fun route(query: String): String {
        val queryLower = query.lowercase()

        val matched = routeConfig.values
            .filter { Regex(it.pattern, RegexOption.IGNORE_CASE).containsMatchIn(queryLower) }
            .sortedByDescending { it.priority }
            .firstOrNull()

        val target = matched?.target ?: "GeneralHandler"

        routeHistory.add(RouteEntry(query, target, System.currentTimeMillis()))

        return target
    }

    fun getRouteHistory(): List<RouteEntry> = routeHistory.toList()

    fun addRoute(pattern: String, target: String, priority: Int = 0) {
        val id = target.lowercase()
        routeConfig[id] = RouteRule(pattern, target, priority)
    }
}
