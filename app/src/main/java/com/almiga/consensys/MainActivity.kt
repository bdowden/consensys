package com.almiga.consensys

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.webkit.JsResult
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.almiga.consensys.ui.theme.ConsensysTheme

/*

*1. WebView w/in app
*2. Inject javascript to alert (maybe more)
*3. textbox for URL to update
*4. invalid URL handling/ other edge cases?
5. Loading indicator - webviewclient DidStartLoad/DidEndLoad
6. unit tests -

 */

class CustomWebViewClient constructor(
    private val javaScriptToExecute: String,
): WebViewClient() {
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        view?.evaluateJavascript(javaScriptToExecute, {})
    }

}

class MainActivity : ComponentActivity() {

    private val javascript = "javascript:window.alert('Hello from android');"

    private val myWebViewClient = CustomWebViewClient(javascript)
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        webView = WebView(
            this,
        ).apply {
            webViewClient = myWebViewClient
            webChromeClient = object : WebChromeClient() {
                override fun onJsAlert(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: JsResult?
                ): Boolean {
                    return super.onJsAlert(view, url, message, result)
                }
            }

            // this should come from a file? not hard coded
            settings.javaScriptEnabled = true


            loadUrl("https://example.com")
        }
        setContent {
            ConsensysTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    var currentUrl by remember { mutableStateOf(TextFieldValue("https://example.com")) }

                    val isValidUrl by remember { derivedStateOf {
                        val isValid = URLUtil.isValidUrl(currentUrl.text)
                        isValid
                    } }

                    /*
                        view state
                         button enabled
                         textIsError
                         Url

                       1. invalid URL - null, empty string, blanks
                                      - invalid protocols



                     */

                    Column(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                isError = !isValidUrl,
                                value = currentUrl,
                                onValueChange = {
                                    // perform validation?
                                    currentUrl = it
                                }
                            )

                            Button(
                                enabled = isValidUrl,
                                onClick = {
                                    // Todo: Validate the URL
                                    webView.loadUrl(currentUrl.text)
                                }
                            ) {
                                Text(
                                    text = "Load URL"
                                )
                            }
                        }
                        AndroidView(
                            modifier = Modifier.padding(innerPadding),
                            factory = {
                                webView
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ConsensysTheme {
        Greeting("Android")
    }
}