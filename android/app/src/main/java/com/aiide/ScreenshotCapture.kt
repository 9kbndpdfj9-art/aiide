package com.aiide

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class ScreenshotCapture(private val context: Context) {

    companion object {
        private const val TAG = "ScreenshotCapture"
    }

    fun capture(view: android.view.View): Bitmap? {
        return try {
            view.isDrawingCacheEnabled = true
            view.buildDrawingCache()
            val bitmap = Bitmap.createBitmap(view.drawingCache)
            view.isDrawingCacheEnabled = false
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Failed to capture screenshot: ${e.message}")
            null
        }
    }

    fun save(bitmap: Bitmap, filename: String = "screenshot_${System.currentTimeMillis()}.png"): String? {
        return try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File(dir, filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save screenshot: ${e.message}")
            null
        }
    }
}
