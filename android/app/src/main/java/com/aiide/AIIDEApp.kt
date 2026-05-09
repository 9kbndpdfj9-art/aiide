package com.aiide

import android.app.Application
import android.webkit.WebView

class AIIDEApp : Application() {
    override fun onCreate() {
        super.onCreate()
        WebView.setWebContentsDebuggingEnabled(true)
    }
}
