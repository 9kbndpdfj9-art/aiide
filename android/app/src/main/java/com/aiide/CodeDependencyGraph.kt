package com.aiide

import android.content.Context
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class CodeDependencyGraph(private val context: Context) {

    private val nodes = ConcurrentHashMap<String, Node>()
    private val edges = mutableListOf<Edge>()

    data class Node(
        val id: String,
        val type: NodeType,
        val name: String,
        val file: String,
        val dependencies: List<String>
    )

    enum class NodeType { CLASS, FUNCTION, VARIABLE, FILE }

    data class Edge(val from: String, val to: String, val type: String)

    fun addNode(node: Node) {
        nodes[node.id] = node
    }

    fun addEdge(from: String, to: String, type: String) {
        edges.add(Edge(from, to, type))
    }

    fun getDependencies(nodeId: String): List<Node> {
        val node = nodes[nodeId] ?: return emptyList()
        return node.dependencies.mapNotNull { nodes[it] }
    }

    fun getDependents(nodeId: String): List<Node> {
        return edges
            .filter { it.to == nodeId }
            .mapNotNull { nodes[it.from] }
    }

    fun findCircularDependencies(): List<List<String>> {
        val cycles = mutableListOf<List<String>>()
        val visited = mutableSetOf<String>()
        val recursionStack = mutableSetOf<String>()

        fun dfs(nodeId: String, path: List<String>): Boolean {
            if (nodeId in recursionStack) {
                val cycleStart = path.indexOf(nodeId)
                if (cycleStart >= 0) {
                    cycles.add(path.subList(cycleStart, path.size) + nodeId)
                }
                return true
            }

            if (nodeId in visited) return false

            visited.add(nodeId)
            recursionStack.add(nodeId)

            val node = nodes[nodeId]
            if (node != null) {
                for (dep in node.dependencies) {
                    if (dfs(dep, path + nodeId)) {
                        return true
                    }
                }
            }

            recursionStack.remove(nodeId)
            return false
        }

        for (nodeId in nodes.keys) {
            visited.clear()
            recursionStack.clear()
            dfs(nodeId, emptyList())
        }

        return cycles
    }
}
