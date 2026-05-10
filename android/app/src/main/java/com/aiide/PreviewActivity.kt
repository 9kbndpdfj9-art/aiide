package com.aiide

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.app.Activity

class PreviewActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var urlInput: EditText
    private lateinit var loadButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        initViews()
        setupWebView()
    }

    private fun initViews() {
        webView = findViewById(R.id.previewWebView)
        urlInput = findViewById(R.id.urlInput)
        loadButton = findViewById(R.id.loadButton)

        loadButton.setOnClickListener {
            val url = urlInput.text.toString()
            if (url.isNotEmpty()) {
                loadUrl(url)
            }
        }
    }

    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
    }

    private fun loadUrl(url: String) {
        val fullUrl = if (url.startsWith("http")) url else "https://$url"
        webView.loadUrl(fullUrl)
    }
}
