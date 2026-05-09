package com.aiide

import android.content.Context
import org.json.JSONObject

class AtomicFileWriter(private val context: Context) {

    fun writeAtomic(path: String, content: String): Boolean {
        return try {
            val tempPath = "$path.tmp"
            java.io.File(tempPath).writeText(content)
            java.io.File(tempPath).renameTo(java.io.File(path))
            true
        } catch (e: Exception) {
            false
        }
    }

    fun writeWithBackup(path: String, content: String): Boolean {
        return try {
            val backupPath = "$path.bak"
            val original = java.io.File(path)
            if (original.exists()) {
                original.copyTo(java.io.File(backupPath), overwrite = true)
            }
            writeAtomic(path, content)
        } catch (e: Exception) {
            false
        }
    }
}
