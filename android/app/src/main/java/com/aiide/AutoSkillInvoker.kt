package com.aiide

import android.content.Context
import org.json.JSONObject

class AutoSkillInvoker(private val context: Context) {

    private val invocationHistory = mutableListOf<InvocationRecord>()

    data class InvocationRecord(
        val skillId: String,
        val trigger: String,
        val timestamp: Long
    )

    fun shouldInvoke(skillId: String, context: Map<String, Any>): Boolean {
        return true
    }

    fun invoke(skillId: String, context: Map<String, Any>): String {
        invocationHistory.add(InvocationRecord(
            skillId = skillId,
            trigger = context["trigger"] as? String ?: "",
            timestamp = System.currentTimeMillis()
        ))
        return "[INVOKED] $skillId"
    }

    fun getHistory(): List<InvocationRecord> = invocationHistory.toList()
}
