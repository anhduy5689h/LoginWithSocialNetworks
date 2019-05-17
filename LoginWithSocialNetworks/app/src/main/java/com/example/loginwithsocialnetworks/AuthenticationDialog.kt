package com.example.loginwithsocialnetworks

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.instagram_login_dialog.*


class AuthenticationDialog(context: Context, private val listener: AuthenticationListener) : Dialog(context) {
    private val TAG = AuthenticationDialog::class.java.simpleName
    private val url = "https://www.google.co.th/"
    //"https://www.instagram.com/oauth/authorize/?client_id=138036f15e3b4ec080af2bc3f32d3f3e&redirect_uri=http://localhost&scope=public_content&response_type=token"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.instagram_login_dialog)

        initializeWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeWebView() {
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        //webView.settings.useWideViewPort = true
        //webView.settings.loadWithOverviewMode = true
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.setSupportMultipleWindows(true)
        webView.clearSslPreferences()
        webView.clearFormData()
        webView.clearHistory()
        webView.clearMatches()
        webView.clearCache(true)
        Log.d(TAG, "url: $url")
        webView.webViewClient = object : WebViewClient() {

            var access_token: String = ""
            var authComplete: Boolean = false

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d(TAG, "onPageFinished url=$url")
                if (url != null && url.contains("#access_token=") && !authComplete) {
                    Log.d(TAG, " inside access_token")
                    val uri = Uri.parse(url)
                    uri.encodedFragment?.let {
                        access_token = it
                        //get the whole token after "=" sign
                        access_token = access_token.substring(access_token.lastIndexOf("=") + 1)
                        Log.d(TAG, "token: $access_token")
                        authComplete = true
                        listener.onTokenReceived(access_token)
                    }
                    dismiss()
                } else if (url != null && url.contains("?error")) {
                    Log.d(TAG, "getting error fetching access token")
                    dismiss()
                } else {
                    Log.d(TAG, "outside both$url")
                }
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                // ignore ssl error
                if (handler != null) {
                    handler.proceed()
                } else {
                    super.onReceivedSslError(view, null, error)
                }
            }
        }
        webView.loadUrl(url)
    }
}

interface AuthenticationListener {
    fun onTokenReceived(auth_token: String)
}