package com.aiide

import android.content.Context
import org.json.JSONObject

class IntentPolishingEngine(private val context: Context) {

    fun polish(intent: String): String {
        var polished = intent.trim()

        polished = polished.replace(Regex("\\s+"), " ")

        if (!polished.endsWith(".") && !polished.endsWith("?")) {
            polished += "."
        }

        return polished
    }
}
