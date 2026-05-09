package com.aiide

import android.content.Context
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class DiffEngine {

    data class DiffResult(
        val additions: Int,
        val deletions: Int,
        val changes: List<Change>
    )

    data class Change(
        val type: ChangeType,
        val content: String,
        val lineNumber: Int
    )

    enum class ChangeType { ADD, DELETE, MODIFY }

    fun computeDiff(oldText: String, newText: String): DiffResult {
        val oldLines = oldText.lines()
        val newLines = newText.lines()

        val changes = mutableListOf<Change>()
        var additions = 0
        var deletions = 0

        val maxLen = maxOf(oldLines.size, newLines.size)
        for (i in 0 until maxLen) {
            val oldLine = oldLines.getOrNull(i)
            val newLine = newLines.getOrNull(i)

            when {
                oldLine == null && newLine != null -> {
                    changes.add(Change(ChangeType.ADD, newLine, i))
                    additions++
                }
                oldLine != null && newLine == null -> {
                    changes.add(Change(ChangeType.DELETE, oldLine, i))
                    deletions++
                }
                oldLine != newLine -> {
                    changes.add(Change(ChangeType.MODIFY, newLine ?: "", i))
                    deletions++
                    additions++
                }
            }
        }

        return DiffResult(additions, deletions, changes)
    }

    fun applyPatch(original: String, patch: String): String {
        val lines = original.lines().toMutableList()
        val patchLines = patch.lines()
        var offset = 0

        for (line in patchLines) {
            when {
                line.startsWith("+") -> {
                    val content = line.substring(1)
                    val index = offset.coerceIn(0, lines.size)
                    lines.add(index, content)
                    offset++
                }
                line.startsWith("-") -> {
                    val index = offset.coerceIn(0, lines.size - 1)
                    lines.removeAt(index)
                }
                line.startsWith(" ") -> {
                    offset++
                }
            }
        }

        return lines.joinToString("\n")
    }
}
