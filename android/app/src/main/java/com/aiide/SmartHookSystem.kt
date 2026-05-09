package com.aiide

import android.content.Context
import android.util.Log
import org.json.JSONObject
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

class SmartHookSystem(private val context: Context) {

    companion object {
        private const val TAG = "SmartHook"
    }

    private val hooks = ConcurrentHashMap<String, HookDefinition>()
    private val hookLock = ReentrantReadWriteLock()

    data class HookDefinition(
        val id: String,
        val name: String,
        val trigger: String,
        val action: String,
        val condition: String?,
        val enabled: Boolean,
        val priority: Int
    )

    data class HookEvent(
        val hookId: String,
        val timestamp: Long,
        val data: Map<String, Any>
    )

    init {
        registerDefaultHooks()
    }

    private fun registerDefaultHooks() {
        registerHook(HookDefinition(
            id = "pre_code_generate",
            name = "代码生成前",
            trigger = "code.generate.before",
            action = "validate_requirements",
            condition = null,
            enabled = true,
            priority = 10
        ))

        registerHook(HookDefinition(
            id = "post_code_generate",
            name = "代码生成后",
            trigger = "code.generate.after",
            action = "validate_syntax",
            condition = null,
            enabled = true,
            priority = 10
        ))

        registerHook(HookDefinition(
            id = "pre_debug",
            name = "调试前",
            trigger = "debug.before",
            action = "gather_context",
            condition = null,
            enabled = true,
            priority = 5
        ))

        Log.i(TAG, "Registered ${hooks.size} default hooks")
    }

    fun registerHook(hook: HookDefinition): Boolean {
        return hookLock.write {
            hooks[hook.id] = hook
            true
        }
    }

    fun unregisterHook(hookId: String): Boolean {
        return hookLock.write {
            hooks.remove(hookId) != null
        }
    }

    fun trigger(hookId: String, data: Map<String, Any>): Boolean {
        val hook = hooks[hookId] ?: return false
        if (!hook.enabled) return false

        return executeHook(hook, data)
    }

    private fun executeHook(hook: HookDefinition, data: Map<String, Any>): Boolean {
        Log.i(TAG, "Executing hook: ${hook.name}")
        return when (hook.action) {
            "validate_requirements" -> validateRequirements(data)
            "validate_syntax" -> validateSyntax(data)
            "gather_context" -> gatherContext(data)
            else -> true
        }
    }

    private fun validateRequirements(data: Map<String, Any>): Boolean {
        val content = data["content"] as? String ?: ""
        return content.isNotEmpty() && content.length >= 10
    }

    private fun validateSyntax(data: Map<String, Any>): Boolean {
        val content = data["content"] as? String ?: ""
        val braceCount = content.count { it == '{' } - content.count { it == '}' }
        return braceCount == 0
    }

    private fun gatherContext(data: Map<String, Any>): Boolean {
        Log.i(TAG, "Gathering context for debugging")
        return true
    }

    fun listHooks(): List<HookDefinition> = hooks.values.toList()

    fun enableHook(hookId: String): Boolean {
        hookLock.write {
            val hook = hooks[hookId] ?: return@write false
            hooks[hookId] = hook.copy(enabled = true)
            true
        }
    }

    fun disableHook(hookId: String): Boolean {
        hookLock.write {
            val hook = hooks[hookId] ?: return@write false
            hooks[hookId] = hook.copy(enabled = false)
            true
        }
    }
}
