package com.aiide

import android.content.Context
import org.json.JSONObject

class TextSearchEngine(private val context: Context) {

    private val index = mutableMapOf<String, IndexedDocument>()

    data class IndexedDocument(
        val id: String,
        val content: String,
        val keywords: Set<String>
    )

    fun indexDocument(id: String, content: String) {
        val keywords = extractKeywords(content)
        index[id] = IndexedDocument(id, content, keywords)
    }

    fun search(query: String): List<SearchResult> {
        val queryKeywords = extractKeywords(query)
        val results = mutableListOf<SearchResult>()

        for (doc in index.values) {
            val matches = doc.keywords.intersect(queryKeywords)
            if (matches.isNotEmpty()) {
                results.add(SearchResult(doc.id, doc.content, matches.size.toDouble()))
            }
        }

        return results.sortedByDescending { it.score }
    }

    private fun extractKeywords(text: String): Set<String> {
        return text.lowercase()
            .split(Regex("[^a-zA-Z0-9]+"))
            .filter { it.length > 2 }
            .toSet()
    }

    data class SearchResult(val id: String, val content: String, val score: Double)
}
