package com.aiide

import android.content.Context

class LiveCodeSession(private val context: Context) {

    private var isActive = false
    private var sessionId: String? = null

    fun startSession(): String {
        sessionId = "session_${System.currentTimeMillis()}"
        isActive = true
        return requireNotNull(sessionId)
    }

    fun endSession() {
        isActive = false
        sessionId = null
    }

    fun executeCode(code: String): String {
        if (!isActive) return "No active session"
        return "[EXECUTED] ${code.take(100)}..."
    }

    fun isSessionActive(): Boolean = isActive
}
