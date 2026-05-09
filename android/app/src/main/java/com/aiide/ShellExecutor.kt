package com.aiide

import android.content.Context
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class ShellExecutor(private val context: Context) {

    private val sessions = ConcurrentHashMap<String, ShellSession>()
    private val sessionLock = Any()

    data class ShellSession(
        val id: String,
        val workingDir: String,
        val envVars: Map<String, String>,
        val startTime: Long
    )

    data class ExecutionResult(
        val exitCode: Int,
        val stdout: String,
        val stderr: String,
        val durationMs: Long
    )

    fun createSession(workingDir: String = "/"): String {
        val sessionId = "session_${System.currentTimeMillis()}_${(Math.random() * 10000).toInt()}"
        sessions[sessionId] = ShellSession(
            id = sessionId,
            workingDir = workingDir,
            envVars = emptyMap(),
            startTime = System.currentTimeMillis()
        )
        return sessionId
    }

    fun execute(command: String, sessionId: String? = null): ExecutionResult {
        val startTime = System.currentTimeMillis()

        return try {
            val sanitizedCommand = sanitizeCommand(command)
            val result = Runtime.getRuntime().exec(arrayOf("/bin/sh", "-c", sanitizedCommand))

            val stdout = result.inputStream.bufferedReader().readText()
            val stderr = result.errorStream.bufferedReader().readText()
            result.waitFor()

            ExecutionResult(
                exitCode = result.exitValue(),
                stdout = stdout,
                stderr = stderr,
                durationMs = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            ExecutionResult(
                exitCode = -1,
                stdout = "",
                stderr = e.message ?: "Unknown error",
                durationMs = System.currentTimeMillis() - startTime
            )
        }
    }

    private fun sanitizeCommand(command: String): String {
        val dangerous = listOf(";", "|", "&", "$", "`", "\n", "\r", "'", "\"", "<", ">")
        var sanitized = command
        for (char in dangerous) {
            sanitized = sanitized.replace(char, '_')
        }
        return sanitized.take(1000)
    }

    fun getSession(sessionId: String): ShellSession? = sessions[sessionId]

    fun closeSession(sessionId: String) {
        sessions.remove(sessionId)
    }

    fun toJson(result: ExecutionResult): String = JSONObject().apply {
        put("exitCode", result.exitCode)
        put("stdout", result.stdout)
        put("stderr", result.stderr)
        put("durationMs", result.durationMs)
    }.toString()
}
