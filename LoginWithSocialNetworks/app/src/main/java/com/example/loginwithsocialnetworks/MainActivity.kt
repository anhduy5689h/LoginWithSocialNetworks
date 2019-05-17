package com.example.loginwithsocialnetworks

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(), AuthenticationListener {
    private val TAG = MainActivity::class.java.simpleName
    private var mCallbackManager: CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeWebView()

        mCallbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(mCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Toast.makeText(this@MainActivity, "Login Success", Toast.LENGTH_LONG).show()
                }

                override fun onCancel() {
                    Toast.makeText(this@MainActivity, "Login Cancel", Toast.LENGTH_LONG).show()
                }

                override fun onError(exception: FacebookException) {
                    Toast.makeText(this@MainActivity, exception.message, Toast.LENGTH_LONG).show()
                }
            })

        btn_facebook_login.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"))
        }

        btn_instagram_login.setOnClickListener {
            /*val authDialog = AuthenticationDialog(this, this)
            authDialog.setCancelable(true)
            //authDialog.window?.setLayout(((getWidth(this) / 100) * 90), LinearLayout.LayoutParams.MATCH_PARENT)
            //authDialog.window?.setGravity(Gravity.CENTER)
            authDialog.show()*/
            initializeWebView()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mCallbackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun getWidth(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    override fun onTokenReceived(auth_token: String) {
        Toast.makeText(this, "Login Success. auth_token=$auth_token", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeWebView() {
        CookieManager.getInstance().setAcceptThirdPartyCookies(webview, true)
        //webView.settings.useWideViewPort = true
        //webView.settings.loadWithOverviewMode = true
        webview.settings.javaScriptEnabled = true
        webview.settings.domStorageEnabled = true
        webview.settings.setSupportMultipleWindows(true)
        webview.clearSslPreferences()
        webview.clearFormData()
        webview.clearHistory()
        webview.clearMatches()
        webview.clearCache(true)
        webview.webViewClient = object : WebViewClient() {

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
                        onTokenReceived(access_token)
                    }
                } else if (url != null && url.contains("?error")) {
                    Log.d(TAG, "getting error fetching access token")
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
        webview.loadUrl("https://www.instagram.com/oauth/authorize/?client_id=138036f15e3b4ec080af2bc3f32d3f3e&redirect_uri=http://localhost&scope=public_content&response_type=token")
    }
}
