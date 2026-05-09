package com.aiide

import android.content.Context
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class IntentAuditor(private val context: Context) {

    private val auditTrail = ConcurrentHashMap<String, AuditEntry>()

    data class AuditEntry(
        val intentId: String,
        val timestamp: Long,
        val intent: String,
        val result: String,
        val score: Double
    )

    fun audit(intentId: String, intent: String, result: String, score: Double) {
        auditTrail[intentId] = AuditEntry(
            intentId = intentId,
            timestamp = System.currentTimeMillis(),
            intent = intent,
            result = result,
            score = score
        )
    }

    fun getAuditTrail(limit: Int = 100): List<AuditEntry> {
        return auditTrail.values.sortedByDescending { it.timestamp }.take(limit)
    }

    fun toJson(entry: AuditEntry): String = JSONObject().apply {
        put("intentId", entry.intentId)
        put("timestamp", entry.timestamp)
        put("intent", entry.intent)
        put("result", entry.result)
        put("score", entry.score)
    }.toString()
}
