package com.aiide

import android.content.Context
import org.json.JSONObject

class IntentSpecGenerator(private val context: Context) {

    fun generate(intent: String): String {
        return JSONObject().apply {
            put("intent", intent)
            put("version", "1.0")
            put("description", "Spec for: $intent")
            put("parameters", JSONObject())
            put("expectedOutput", "")
        }.toString(2)
    }
}
