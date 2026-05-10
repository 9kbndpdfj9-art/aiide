package com.aiide

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AnthropicProvider(context: Context) : ModelProvider(context) {

    companion object {
        private const val BASE_URL = "https://api.anthropic.com/v1"
    }

    fun generate(prompt: String, maxTokens: Int = 1024): String {
        return "[CLAUDE_RESPONSE] Prompt: ${prompt.take(100)}..."
    }

    fun generateWithSystem(prompt: String, system: String, maxTokens: Int = 1024): String {
        return "[CLAUDE_RESPONSE] System: ${system.take(50)}... Prompt: ${prompt.take(100)}..."
    }
}
