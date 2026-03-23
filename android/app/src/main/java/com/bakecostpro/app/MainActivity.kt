package com.bakecostpro.app

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    // File chooser launcher for CSV import
    private val fileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            filePathCallback?.onReceiveValue(if (uri != null) arrayOf(uri) else emptyArray())
            filePathCallback = null
        }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Immersive layout — app fills behind the status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }

        webView = WebView(this)
        setContentView(webView)

        // ── WebView settings ──────────────────────────────────────────
        webView.settings.apply {
            javaScriptEnabled          = true
            domStorageEnabled          = true      // localStorage
            databaseEnabled            = true
            allowFileAccess            = true
            allowContentAccess         = true
            setSupportZoom(false)
            builtInZoomControls        = false
            displayZoomControls        = false
            loadWithOverviewMode       = true
            useWideViewPort            = true
            cacheMode                  = WebSettings.LOAD_DEFAULT
            // Required for Anthropic API + Google Sheets (both HTTPS)
            @Suppress("DEPRECATION")
            mixedContentMode           = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            userAgentString            = userAgentString + " BakeCostProApp/1.0"
        }

        // ── WebViewClient ─────────────────────────────────────────────
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val url = request.url.toString()
                // Keep asset URLs and API calls inside WebView
                if (url.startsWith("file://") ||
                    url.contains("api.anthropic.com") ||
                    url.contains("script.google.com")) {
                    return false
                }
                // Open all other HTTP/HTTPS URLs in the system browser
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    return true
                }
                return false
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                super.onReceivedError(view, request, error)
                // Only show error for main page failures
                if (request.isForMainFrame) {
                    Toast.makeText(
                        this@MainActivity,
                        "Page error: ${error.description}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // ── WebChromeClient — handles JS dialogs + file chooser ──────
        webView.webChromeClient = object : WebChromeClient() {

            override fun onJsAlert(
                view: WebView?, url: String?, message: String?, result: JsResult?
            ): Boolean {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage(message)
                    .setPositiveButton("OK") { _, _ -> result?.confirm() }
                    .setOnCancelListener { result?.cancel() }
                    .show()
                return true
            }

            override fun onJsConfirm(
                view: WebView?, url: String?, message: String?, result: JsResult?
            ): Boolean {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage(message)
                    .setPositiveButton("OK")     { _, _ -> result?.confirm() }
                    .setNegativeButton("Cancel") { _, _ -> result?.cancel()  }
                    .setOnCancelListener         { result?.cancel() }
                    .show()
                return true
            }

            override fun onJsPrompt(
                view: WebView?, url: String?, message: String?,
                defaultValue: String?, result: JsPromptResult?
            ): Boolean {
                val input = android.widget.EditText(this@MainActivity)
                input.setText(defaultValue)
                AlertDialog.Builder(this@MainActivity)
                    .setMessage(message)
                    .setView(input)
                    .setPositiveButton("OK")     { _, _ -> result?.confirm(input.text.toString()) }
                    .setNegativeButton("Cancel") { _, _ -> result?.cancel() }
                    .show()
                return true
            }

            // File chooser for CSV import
            override fun onShowFileChooser(
                view: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                params: FileChooserParams?
            ): Boolean {
                filePathCallback?.onReceiveValue(null)
                filePathCallback = callback
                fileChooserLauncher.launch("*/*")
                return true
            }
        }

        // ── Download listener — handle CSV export ────────────────────
        webView.setDownloadListener { url, _, contentDisposition, mimetype, _ ->
            try {
                if (url.startsWith("blob:") || url.startsWith("data:")) {
                    // Inject JS to handle blob/data URLs via the page
                    webView.evaluateJavascript(
                        """
                        (function() {
                          var a = document.createElement('a');
                          a.href = '$url';
                          a.download = 'BakeCost_Export.csv';
                          document.body.appendChild(a);
                          a.click();
                          document.body.removeChild(a);
                        })();
                        """.trimIndent(), null
                    )
                } else {
                    // Regular URL — use DownloadManager
                    val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val req = DownloadManager.Request(Uri.parse(url)).apply {
                        setMimeType(mimetype)
                        setDescription("Downloading via BakeCost Pro")
                        setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype))
                        setNotificationVisibility(
                            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                        )
                        setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS,
                            URLUtil.guessFileName(url, contentDisposition, mimetype)
                        )
                    }
                    dm.enqueue(req)
                    Toast.makeText(this, "Download started…", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // ── Add JS bridge for Android-specific features ───────────────
        webView.addJavascriptInterface(AndroidBridge(this), "AndroidBridge")

        // ── Load the app ──────────────────────────────────────────────
        webView.loadUrl("file:///android_asset/index.html")
    }

    /** Handle Android back button — go back in WebView history first */
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}

// ── JS ↔ Android bridge ───────────────────────────────────────────────────
class AndroidBridge(private val context: Context) {

    @JavascriptInterface
    fun showToast(message: String) {
        (context as? AppCompatActivity)?.runOnUiThread {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun getDeviceInfo(): String {
        return "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }
}
