package com.aiide

import android.webkit.WebView
import android.webkit.WebViewClient
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.pm.PackageManager
import android.Manifest
import android.content.pm.PackageInfo
import org.json.JSONObject
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var inputField: EditText
    private lateinit var sendButton: Button
    private lateinit var statusText: TextView
    private val executor = Executors.newCachedThreadPool()
    private var bridge: Bridge? = null
    private var modelRouter: ModelRouter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initComponents()
        setupWebView()
        setupBridge()
        loadWebInterface()
    }

    private fun initComponents() {
        webView = findViewById(R.id.webView)
        inputField = findViewById(R.id.inputField)
        sendButton = findViewById(R.id.sendButton)
        statusText = findViewById(R.id.statusText)

        sendButton.setOnClickListener {
            val input = inputField.text.toString()
            if (input.isNotEmpty()) {
                processInput(input)
            }
        }
    }

    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
    }

    private fun setupBridge() {
        try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                appInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                appInfo.versionCode.toLong()
            }

            modelRouter = ModelRouter(this)
            bridge = Bridge(this, modelRouter!!)

            val config = JSONObject().apply {
                put("platform", "android")
                put("version", versionCode)
                put("capabilities", JSONObject().apply {
                    put("multimodal", true)
                    put("voice", true)
                    put("camera", true)
                    put("file_access", true)
                })
            }

            bridge?.initialize(config)
            updateStatus("Bridge initialized successfully")
        } catch (e: Exception) {
            updateStatus("Bridge init error: ${e.message}")
        }
    }

    private fun loadWebInterface() {
        webView.loadUrl("file:///android_asset/www/index.html")
    }

    private fun processInput(input: String) {
        executor.execute {
            try {
                val result = bridge?.dispatch(input)
                runOnUiThread {
                    inputField.setText("")
                    webView.evaluateJavascript("window.receiveMessage($result)", null)
                }
            } catch (e: Exception) {
                updateStatus("Error: ${e.message}")
            }
        }
    }

    private fun updateStatus(message: String) {
        runOnUiThread {
            statusText.text = message
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }
}
