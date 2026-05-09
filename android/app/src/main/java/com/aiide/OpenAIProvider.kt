package com.aiide

import android.content.Context
import org.json.JSONObject

class OpenAIProvider(private val context: Context) : ModelProvider(context) {

    companion object {
        private const val BASE_URL = "https://api.openai.com/v1"
    }

    fun generate(prompt: String, model: String = "gpt-3.5-turbo", maxTokens: Int = 1024): String {
        return "[GPT_RESPONSE] Prompt: ${prompt.take(100)}..."
    }

    fun chat(messages: List<Message>, model: String = "gpt-3.5-turbo"): String {
        return "[GPT_CHAT_RESPONSE] ${messages.size} messages"
    }

    data class Message(val role: String, val content: String)
}
