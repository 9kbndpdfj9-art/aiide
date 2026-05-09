package com.aiide

import android.content.Context
import org.json.JSONObject

class WebSearchEngine {

    data class SearchResult(
        val title: String,
        val url: String,
        val snippet: String,
        val relevance: Double
    )

    fun search(query: String, maxResults: Int = 10): List<SearchResult> {
        return listOf(
            SearchResult(
                title = "Android Developer Documentation",
                url = "https://developer.android.com",
                snippet = "Official Android development resources",
                relevance = 0.95
            ),
            SearchResult(
                title = "Kotlin Documentation",
                url = "https://kotlinlang.org",
                snippet = "Kotlin programming language official docs",
                relevance = 0.90
            )
        ).take(maxResults)
    }

    fun toJson(results: List<SearchResult>): String = JSONObject().apply {
        put("query", "")
        put("results", org.json.JSONArray().apply {
            results.forEach { result ->
                put(JSONObject().apply {
                    put("title", result.title)
                    put("url", result.url)
                    put("snippet", result.snippet)
                    put("relevance", result.relevance)
                })
            }
        })
    }.toString()
}
