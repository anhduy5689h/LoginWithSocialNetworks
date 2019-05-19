package com.example.loginwithsocialnetworks

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.twitter.sdk.android.core.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import com.twitter.sdk.android.core.TwitterAuthToken
import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.core.TwitterSession


class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private var mCallbackManager: CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeTwitter()
        setContentView(R.layout.activity_main)

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
            initializeInstagramWebView()
        }

        setCallbackTwitterLogin()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == TwitterAuthConfig.DEFAULT_AUTH_REQUEST_CODE) {
            btn_twitter_login.onActivityResult(requestCode, resultCode, data)
        } else if (FacebookSdk.isFacebookRequestCode(requestCode)) {
            mCallbackManager?.onActivityResult(requestCode, resultCode, data)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    //===========================INSTAGRAM================================

    override fun onBackPressed() {
        if (lin_webview_instagram.visibility == View.VISIBLE) {
            clearCacheWebView()
            lin_webview_instagram.visibility = View.INVISIBLE
        } else {
            super.onBackPressed()
        }
    }

    private fun clearCacheWebView() {
        webview_instagram.clearFormData()
        webview_instagram.clearHistory()
        webview_instagram.clearMatches()
        webview_instagram.clearCache(true)
        //Clear previous WebView's content before loading the WebView again
        webview_instagram.loadUrl("about:blank")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeInstagramWebView() {
        lin_webview_instagram.visibility = View.VISIBLE
        //
        CookieManager.getInstance().setAcceptThirdPartyCookies(webview_instagram, true)
        // Call the code below if you want to logout instagram
        // or if you don't want CookieManager cache your instagram account for the second login
        //CookieManager.getInstance().removeAllCookies(null)
        webview_instagram.settings.useWideViewPort = true
        webview_instagram.settings.loadWithOverviewMode = true
        webview_instagram.settings.javaScriptEnabled = true
        webview_instagram.settings.domStorageEnabled = true
        clearCacheWebView()
        webview_instagram.webViewClient = object : WebViewClient() {
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
                        onInstagramTokenReceived(access_token)
                        clearCacheWebView()
                        lin_webview_instagram.visibility = View.INVISIBLE
                    }
                } else if (url != null && url.contains("?error")) {
                    Log.d(TAG, "getting error fetching access token")
                    lin_webview_instagram.visibility = View.GONE
                } else {
                    Log.d(TAG, "outside both$url")
                }
            }
        }
        webview_instagram.loadUrl("https://www.instagram.com/oauth/authorize/?client_id=138036f15e3b4ec080af2bc3f32d3f3e&redirect_uri=http://localhost&scope=public_content&response_type=token")
    }

    fun onInstagramTokenReceived(auth_token: String) {
        Toast.makeText(this, "Login Success. auth_token=$auth_token", Toast.LENGTH_SHORT).show()
    }

    //========================TWITTER============================

    private var twitterConfig: TwitterConfig? = null

    /**
     * The method must be called in onCreate() of Activity and before setContentView(),
     * else the login button will be disabled
     */
    private fun initializeTwitter() {
        if (twitterConfig == null) {
            twitterConfig = TwitterConfig.Builder(this)
                .logger(DefaultLogger(Log.DEBUG))//enable logging when app is in debug mode
                .twitterAuthConfig(
                    TwitterAuthConfig(
                        getString(R.string.com_twitter_sdk_android_CONSUMER_KEY),
                        getString(R.string.com_twitter_sdk_android_CONSUMER_SECRET)
                    )
                )//pass the created app Consumer KEY and Secret also called API Key and Secret
                .debug(true)//enable debug mode
                .build()
            //finally initialize twitter with created configs
            Twitter.initialize(twitterConfig)
        }
    }

    /**
     * The method is called in onCreate() and after setContentView()
     */
    private fun setCallbackTwitterLogin() {
        //If you see the error:
        //Callback URL not approved for this client application. Approved callback URLs can be adjusted in your application settings.
        //Add twittersdk:// as one callback URL on your twitter app setting https://apps.twitter.com/
        btn_twitter_login.callback = object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>?) {
                val session = TwitterCore.getInstance().sessionManager.activeSession
                val authToken = session.authToken
                val token = authToken.token
                val secret = authToken.secret
                Toast.makeText(this@MainActivity, "Twitter $authToken", Toast.LENGTH_LONG).show()
            }

            override fun failure(exception: TwitterException?) {
                Toast.makeText(this@MainActivity, "Twitter login failed", Toast.LENGTH_LONG).show()
            }
        }
    }
}
