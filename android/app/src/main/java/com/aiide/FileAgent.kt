package com.aiide

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class FileAgent(private val context: Context) {

    private val fileCache = ConcurrentHashMap<String, FileInfo>()

    data class FileInfo(
        val path: String,
        val content: String,
        val timestamp: Long,
        val size: Long
    )

    fun readFile(path: String): String? {
        return try {
            val file = File(path)
            if (file.exists() && file.canRead()) {
                val content = file.readText()
                fileCache[path] = FileInfo(path, content, System.currentTimeMillis(), file.length())
                content
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun writeFile(path: String, content: String): Boolean {
        return try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.writeText(content)
            fileCache[path] = FileInfo(path, content, System.currentTimeMillis(), file.length())
            true
        } catch (e: Exception) {
            false
        }
    }

    fun listFiles(directory: String): List<String> {
        return try {
            val dir = File(directory)
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()?.map { it.absolutePath } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deleteFile(path: String): Boolean {
        return try {
            val file = File(path)
            file.delete()
        } catch (e: Exception) {
            false
        }
    }

    fun fileExists(path: String): Boolean = File(path).exists()

    fun getFileInfo(path: String): JSONObject? {
        return try {
            val file = File(path)
            if (file.exists()) {
                JSONObject().apply {
                    put("path", file.absolutePath)
                    put("size", file.length())
                    put("modified", file.lastModified())
                    put("isDirectory", file.isDirectory)
                    put("canRead", file.canRead())
                    put("canWrite", file.canWrite())
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
