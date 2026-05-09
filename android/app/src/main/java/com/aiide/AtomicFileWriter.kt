package com.aiide

import android.content.Context
import org.json.JSONObject

class AtomicFileWriter(private val context: Context) {

    fun writeAtomic(path: String, content: String): Boolean {
        return try {
            val targetFile = java.io.File(path)
            val tempFile = java.io.File("$path.tmp")

            tempFile.writeText(content)
            if (!tempFile.renameTo(targetFile)) {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }

            targetFile.exists() && targetFile.readText() == content
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
