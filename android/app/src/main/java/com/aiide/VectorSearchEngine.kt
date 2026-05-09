package com.aiide

import android.content.Context
import org.json.JSONObject

class VectorSearchEngine {

    private val index = mutableMapOf<String, VectorEntry>()

    data class VectorEntry(
        val id: String,
        val content: String,
        val vector: FloatArray,
        val metadata: Map<String, String>
    )

    data class SearchResult(
        val id: String,
        val content: String,
        val score: Double,
        val metadata: Map<String, String>
    )

    fun index(id: String, content: String, metadata: Map<String, String> = emptyMap()) {
        val vector = computeEmbedding(content)
        index[id] = VectorEntry(id, content, vector, metadata)
    }

    fun search(query: String, topK: Int = 10): List<SearchResult> {
        val queryVector = computeEmbedding(query)

        return index.values
            .map { entry ->
                val score = cosineSimilarity(queryVector, entry.vector)
                SearchResult(entry.id, entry.content, score, entry.metadata)
            }
            .sortedByDescending { it.score }
            .take(topK)
    }

    private fun computeEmbedding(text: String): FloatArray {
        val words = text.lowercase().split(Regex("\\s+"))
        val uniqueWords = words.distinct()
        return FloatArray(128) { i ->
            if (i < uniqueWords.size) {
                uniqueWords[i].hashCode().toFloat() / Int.MAX_VALUE
            } else 0f
        }
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Double {
        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0

        for (i in a.indices) {
            dotProduct += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }

        return if (normA > 0 && normB > 0) {
            dotProduct / (Math.sqrt(normA) * Math.sqrt(normB))
        } else 0.0
    }

    fun toJson(result: SearchResult): String = JSONObject().apply {
        put("id", result.id)
        put("content", result.content)
        put("score", result.score)
        put("metadata", JSONObject(result.metadata))
    }.toString()
}
