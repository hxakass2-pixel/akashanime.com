package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeWebBrowserScreen(
    initialUrl: String = "https://www.crunchyroll.com",
    onAddCurrentUrlToLibrary: (url: String, title: String) -> Unit,
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    var currentUrl by remember { mutableStateOf(initialUrl) }
    var inputUrl by remember { mutableStateOf(initialUrl) }
    var pageTitle by remember { mutableStateOf("Anime Web Portal") }
    var isLoading by remember { mutableStateOf(false) }
    var progressVal by remember { mutableFloatStateOf(0f) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    // Quick anime websites bookmarks
    val quickSites = listOf(
        "Crunchyroll" to "https://www.crunchyroll.com",
        "AniList" to "https://anilist.co",
        "MyAnimeList" to "https://myanimelist.net",
        "HiDive" to "https://www.hidive.com",
        "AnimeNewsNetwork" to "https://www.animenewsnetwork.com",
        "YouTube Anime" to "https://www.youtube.com/results?search_query=anime+official+trailer"
    )

    BackHandler(enabled = canGoBack) {
        webViewRef?.goBack()
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                // Main URL Bar & Nav Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (canGoBack) webViewRef?.goBack() else onBackToHome()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }

                    IconButton(
                        onClick = { webViewRef?.goForward() },
                        enabled = canGoForward
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward")
                    }

                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("browser_url_input"),
                        shape = RoundedCornerShape(26.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                var formatted = inputUrl.trim()
                                if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
                                    formatted = if (formatted.contains(".")) "https://$formatted" else "https://www.google.com/search?q=$formatted+anime"
                                }
                                currentUrl = formatted
                                webViewRef?.loadUrl(formatted)
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Go", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )

                    IconButton(onClick = { webViewRef?.reload() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload")
                    }

                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = "Open in Chrome")
                    }
                }

                // Progress Indicator
                if (isLoading) {
                    LinearProgressIndicator(
                        progress = { progressVal },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Quick Website Shortcut Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickSites.forEach { (name, link) ->
                        SuggestionChip(
                            onClick = {
                                inputUrl = link
                                currentUrl = link
                                webViewRef?.loadUrl(link)
                            },
                            label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                            icon = { Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    onAddCurrentUrlToLibrary(currentUrl, pageTitle)
                },
                modifier = Modifier.testTag("save_current_web_anime_button"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.BookmarkAdd, contentDescription = null) },
                text = { Text("Save Link to Anime") }
            )
        }
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        userAgentString = settings.userAgentString.replace("; wv", "") // modern user-agent
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                            url?.let {
                                currentUrl = it
                                inputUrl = it
                            }
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                            canGoBack = view?.canGoBack() == true
                            canGoForward = view?.canGoForward() == true
                            pageTitle = view?.title ?: "Anime Website"
                        }

                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            return false // Load in WebView
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            progressVal = newProgress / 100f
                            if (newProgress >= 100) isLoading = false
                        }

                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            super.onReceivedTitle(view, title)
                            if (!title.isNullOrBlank()) {
                                pageTitle = title
                            }
                        }
                    }

                    loadUrl(initialUrl)
                    webViewRef = this
                }
            },
            update = { view ->
                webViewRef = view
            }
        )
    }
}
