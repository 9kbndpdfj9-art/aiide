package com.aiide

import android.content.Context
import org.json.JSONObject

class SleepModeEngine(private val context: Context) {

    private var isActive = true
    private var lastActivity = System.currentTimeMillis()

    fun checkActivity(): Boolean {
        if (!isActive) return false

        val idleTime = System.currentTimeMillis() - lastActivity
        if (idleTime > 30 * 60 * 1000) {
            enterSleepMode()
        }
        return isActive
    }

    fun enterSleepMode() {
        isActive = false
    }

    fun wakeUp() {
        isActive = true
        lastActivity = System.currentTimeMillis()
    }

    fun recordActivity() {
        lastActivity = System.currentTimeMillis()
        if (!isActive) wakeUp()
    }
}
