package com.example.ui.hooks

import android.annotation.SuppressLint
import android.content.Context
import android.util.Base64
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun rememberBrowserGeolocation(
    onLocationFetched: (Double, Double) -> Unit,
    onError: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.setGeolocationEnabled(true)
            webChromeClient = object : WebChromeClient() {
                override fun onGeolocationPermissionsShowPrompt(
                    origin: String?,
                    callback: GeolocationPermissions.Callback?
                ) {
                    callback?.invoke(origin, true, false)
                }
            }
            addJavascriptInterface(object {
                @android.webkit.JavascriptInterface
                fun onLocationSuccess(lat: Double, lon: Double) {
                    coroutineScope.launch(Dispatchers.Main) {
                        onLocationFetched(lat, lon)
                    }
                }

                @android.webkit.JavascriptInterface
                fun onLocationError(errorMsg: String) {
                    coroutineScope.launch(Dispatchers.Main) {
                        onError(errorMsg)
                    }
                }
            }, "AndroidBridge")
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.destroy()
        }
    }

    return {
        coroutineScope.launch(Dispatchers.Main) {
            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Geolocation</title>
                    <script type="text/javascript">
                        function getGeoLocation() {
                            if (navigator.geolocation) {
                                navigator.geolocation.getCurrentPosition(
                                    function(position) {
                                        AndroidBridge.onLocationSuccess(position.coords.latitude, position.coords.longitude);
                                    },
                                    function(error) {
                                        // Retry with low accuracy for environments without GPS hardware
                                        navigator.geolocation.getCurrentPosition(
                                            function(pos) {
                                                AndroidBridge.onLocationSuccess(pos.coords.latitude, pos.coords.longitude);
                                            },
                                            function(err) {
                                                AndroidBridge.onLocationError("Geolocation failed: " + err.message);
                                            },
                                            { enableHighAccuracy: false, timeout: 5000, maximumAge: 60000 }
                                        );
                                    },
                                    { enableHighAccuracy: true, timeout: 5000, maximumAge: 0 }
                                );
                            } else {
                                AndroidBridge.onLocationError("Browser Geolocation API not supported.");
                            }
                        }
                    </script>
                </head>
                <body onload="getGeoLocation()">
                </body>
                </html>
            """.trimIndent()
            webView.loadDataWithBaseURL(
                "https://localhost",
                html,
                "text/html",
                "utf-8",
                null
            )
        }
    }
}
