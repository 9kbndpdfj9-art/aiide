package com.aiide

import android.content.Context
import org.json.JSONObject

class ModelProvider(private val context: Context) {
    private val providers = mutableMapOf<String, ProviderConfig>()

    data class ProviderConfig(
        val name: String,
        val apiKey: String,
        val baseUrl: String,
        val enabled: Boolean
    )

    fun registerProvider(name: String, config: ProviderConfig) {
        providers[name] = config
    }

    fun getProvider(name: String): ProviderConfig? = providers[name]

    fun listProviders(): List<String> = providers.keys.toList()

    fun isEnabled(name: String): Boolean = providers[name]?.enabled ?: false
}
