package com.aiide

import android.content.Context
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

class AutoDebugger(private val context: Context) {

    private val sessions = ConcurrentHashMap<String, DebugSession>()
    private val sessionLock = ReentrantReadWriteLock()

    data class DebugSession(
        val id: String,
        val errorMessage: String,
        val stackTrace: String,
        val filePath: String,
        val lineNumber: Int,
        val timestamp: Long,
        val fixes: List<String>
    )

    data class FixResult(
        val success: Boolean,
        val fix: String?,
        val confidence: Double,
        val explanation: String
    )

    fun analyzeError(errorMessage: String, stackTrace: String): FixResult {
        val sessionId = generateSessionId()

        sessionLock.write {
            sessions[sessionId] = DebugSession(
                id = sessionId,
                errorMessage = errorMessage,
                stackTrace = stackTrace,
                filePath = extractFilePath(stackTrace),
                lineNumber = extractLineNumber(stackTrace),
                timestamp = System.currentTimeMillis(),
                fixes = emptyList()
            )
        }

        return FixResult(
            success = true,
            fix = generateFix(errorMessage, stackTrace),
            confidence = 0.85,
            explanation = "Based on error analysis"
        )
    }

    private fun generateFix(errorMessage: String, stackTrace: String): String {
        val fixes = mutableListOf<String>()

        when {
            errorMessage.contains("NullPointerException") -> {
                fixes.add("检查对象是否为null后再使用")
                fixes.add("使用安全调用操作符 (?.) ")
            }
            errorMessage.contains("ArrayIndexOutOfBoundsException") -> {
                fixes.add("检查数组索引是否在有效范围内")
                fixes.add("使用forEach或迭代器替代索引访问")
            }
            errorMessage.contains("ClassNotFoundException") -> {
                fixes.add("检查类名拼写是否正确")
                fixes.add("确保依赖已正确添加到build.gradle")
            }
            else -> {
                fixes.add("检查错误信息并定位问题")
                fixes.add("查看相关文件和堆栈跟踪")
            }
        }

        return fixes.joinToString("\n")
    }

    private fun extractFilePath(stackTrace: String): String {
        val atRegex = Regex("""at\s+[\w.]+\.(\w+)\.(\w+)\(([^:]+):(\d+)""")
        val match = atRegex.find(stackTrace)
        return match?.groupValues?.getOrNull(3) ?: "Unknown"
    }

    private fun extractLineNumber(stackTrace: String): Int {
        val atRegex = Regex("""at\s+[\w.]+\.(\w+)\.(\w+)\(([^:]+):(\d+)""")
        val match = atRegex.find(stackTrace)
        return match?.groupValues?.getOrNull(4)?.toIntOrNull() ?: 0
    }

    private fun generateSessionId(): String {
        return "debug_${System.currentTimeMillis()}_${(Math.random() * 10000).toInt()}"
    }

    fun getSession(sessionId: String): DebugSession? = sessions[sessionId]

    fun listSessions(): List<DebugSession> = sessions.values.toList()
}
