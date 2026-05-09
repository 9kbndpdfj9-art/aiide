package com.aiide

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class IntentGraph(private val context: Context) {

    companion object {
        private const val TAG = "IntentGraph"
    }

    private val nodes = ConcurrentHashMap<String, IntentNode>()
    private val edges = mutableListOf<IntentEdge>()

    data class IntentNode(
        val id: String,
        val intent: String,
        val description: String,
        val keywords: List<String>,
        val parentId: String?,
        val children: List<String>
    )

    data class IntentEdge(
        val fromId: String,
        val toId: String,
        val relation: String
    )

    init {
        initializeDefaultGraph()
    }

    private fun initializeDefaultGraph() {
        val rootNode = IntentNode(
            id = "root",
            intent = "root",
            description = "Root intent",
            keywords = emptyList(),
            parentId = null,
            children = listOf("coding", "debugging", "analysis", "search")
        )
        nodes["root"] = rootNode

        val codingNode = IntentNode(
            id = "coding",
            intent = "coding",
            description = "Coding and implementation tasks",
            keywords = listOf("代码", "生成", "实现", "code"),
            parentId = "root",
            children = listOf("code_generate", "code_review", "refactor")
        )
        nodes["coding"] = codingNode

        Log.i(TAG, "IntentGraph initialized with ${nodes.size} nodes")
    }

    fun addNode(node: IntentNode) {
        nodes[node.id] = node
    }

    fun addEdge(fromId: String, toId: String, relation: String) {
        edges.add(IntentEdge(fromId, toId, relation))
    }

    fun findPath(fromIntent: String, toIntent: String): List<String> {
        val fromNode = nodes.values.find { it.intent == fromIntent }
        val toNode = nodes.values.find { it.intent == toIntent }

        if (fromNode == null || toNode == null) return emptyList()

        return findPathBFS(fromNode.id, toNode.id)
    }

    private fun findPathBFS(fromId: String, toId: String): List<String> {
        val visited = mutableSetOf<String>()
        val queue = ArrayDeque<Pair<String, List<String>>>()
        queue.add(fromId to listOf(fromId))

        while (queue.isNotEmpty()) {
            val (current, path) = queue.removeFirst()
            if (current == toId) return path

            if (current in visited) continue
            visited.add(current)

            val children = nodes[current]?.children ?: emptyList()
            for (child in children) {
                if (child !in visited) {
                    queue.add(child to path + child)
                }
            }
        }

        return emptyList()
    }

    fun toJson(): String = JSONObject().apply {
        put("nodes", org.json.JSONArray().apply {
            nodes.values.forEach { node ->
                put(JSONObject().apply {
                    put("id", node.id)
                    put("intent", node.intent)
                    put("description", node.description)
                    put("keywords", org.json.JSONArray(node.keywords))
                })
            }
        })
        put("edges", org.json.JSONArray().apply {
            edges.forEach { edge ->
                put(JSONObject().apply {
                    put("from", edge.fromId)
                    put("to", edge.toId)
                    put("relation", edge.relation)
                })
            }
        })
    }.toString()
}
