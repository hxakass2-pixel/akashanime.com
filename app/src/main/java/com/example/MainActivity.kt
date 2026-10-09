package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AnimeItem
import com.example.ui.AnimeViewModel
import com.example.ui.components.AddAnimeDialog
import com.example.ui.components.AnimeDetailDialog
import com.example.ui.screens.AnimeLibraryScreen
import com.example.ui.screens.AnimeWebBrowserScreen
import com.example.ui.screens.ChatbotScreen
import com.example.ui.screens.ExploreWebHubScreen
import com.example.ui.screens.ImageAnalyzerScreen
import com.example.ui.theme.AkashAnimeTheme

enum class AkashAnimeTab {
    LIBRARY,
    WEB_BROWSER,
    VISION_AI,
    CHATBOT,
    EXPLORE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AkashAnimeTheme {
                AkashAnimeApp()
            }
        }
    }
}

@Composable
fun AkashAnimeApp(viewModel: AnimeViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(AkashAnimeTab.LIBRARY) }
    var browserUrl by remember { mutableStateOf("https://www.crunchyroll.com") }

    val isAddDialogOpen by viewModel.isAddDialogOpen.collectAsState()
    val selectedAnimeDetail by viewModel.selectedAnimeForDetail.collectAsState()
    var editingAnime by remember { mutableStateOf<AnimeItem?>(null) }

    // Custom back handler for sub-tabs
    BackHandler(enabled = selectedTab != AkashAnimeTab.LIBRARY) {
        selectedTab = AkashAnimeTab.LIBRARY
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == AkashAnimeTab.LIBRARY,
                    onClick = { selectedTab = AkashAnimeTab.LIBRARY },
                    icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "My Links") },
                    label = { Text("My Links") },
                    modifier = Modifier.testTag("nav_library")
                )
                NavigationBarItem(
                    selected = selectedTab == AkashAnimeTab.WEB_BROWSER,
                    onClick = { selectedTab = AkashAnimeTab.WEB_BROWSER },
                    icon = { Icon(Icons.Default.Language, contentDescription = "akashanime.com") },
                    label = { Text("Web Portal") },
                    modifier = Modifier.testTag("nav_web_browser")
                )
                NavigationBarItem(
                    selected = selectedTab == AkashAnimeTab.VISION_AI,
                    onClick = { selectedTab = AkashAnimeTab.VISION_AI },
                    icon = { Icon(Icons.Default.DocumentScanner, contentDescription = "Vision AI") },
                    label = { Text("AI Vision") },
                    modifier = Modifier.testTag("nav_vision_ai")
                )
                NavigationBarItem(
                    selected = selectedTab == AkashAnimeTab.CHATBOT,
                    onClick = { selectedTab = AkashAnimeTab.CHATBOT },
                    icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI Sensei") },
                    label = { Text("AI Sensei") },
                    modifier = Modifier.testTag("nav_chatbot")
                )
                NavigationBarItem(
                    selected = selectedTab == AkashAnimeTab.EXPLORE,
                    onClick = { selectedTab = AkashAnimeTab.EXPLORE },
                    icon = { Icon(Icons.Default.Explore, contentDescription = "Web Hub") },
                    label = { Text("Web Hub") },
                    modifier = Modifier.testTag("nav_explore")
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                AkashAnimeTab.LIBRARY -> {
                    AnimeLibraryScreen(
                        viewModel = viewModel,
                        onOpenWebBrowser = { url ->
                            browserUrl = url
                            selectedTab = AkashAnimeTab.WEB_BROWSER
                        },
                        onOpenAddDialog = {
                            editingAnime = null
                            viewModel.isAddDialogOpen.value = true
                        }
                    )
                }
                AkashAnimeTab.WEB_BROWSER -> {
                    AnimeWebBrowserScreen(
                        initialUrl = browserUrl,
                        onAddCurrentUrlToLibrary = { url, title ->
                            viewModel.isAddDialogOpen.value = true
                            viewModel.autoFillFromLinkOrName(url) { data ->
                                // auto-filled
                            }
                        },
                        onBackToHome = {
                            selectedTab = AkashAnimeTab.LIBRARY
                        }
                    )
                }
                AkashAnimeTab.VISION_AI -> {
                    ImageAnalyzerScreen(
                        viewModel = viewModel,
                        onAnimeAdded = {
                            selectedTab = AkashAnimeTab.LIBRARY
                        }
                    )
                }
                AkashAnimeTab.CHATBOT -> {
                    ChatbotScreen(
                        viewModel = viewModel
                    )
                }
                AkashAnimeTab.EXPLORE -> {
                    ExploreWebHubScreen(
                        viewModel = viewModel,
                        onOpenWebBrowser = { url ->
                            browserUrl = url
                            selectedTab = AkashAnimeTab.WEB_BROWSER
                        },
                        onAddAnimeWithLink = { url, title ->
                            viewModel.isAddDialogOpen.value = true
                        }
                    )
                }
            }
        }

        // Add or Edit Anime Dialog
        if (isAddDialogOpen) {
            AddAnimeDialog(
                viewModel = viewModel,
                initialAnime = editingAnime,
                onDismiss = {
                    viewModel.isAddDialogOpen.value = false
                    editingAnime = null
                }
            )
        }

        // Anime Detail Modal
        selectedAnimeDetail?.let { anime ->
            AnimeDetailDialog(
                anime = anime,
                viewModel = viewModel,
                onOpenWebBrowser = { url ->
                    viewModel.selectedAnimeForDetail.value = null
                    browserUrl = url
                    selectedTab = AkashAnimeTab.WEB_BROWSER
                },
                onEdit = {
                    editingAnime = anime
                    viewModel.selectedAnimeForDetail.value = null
                    viewModel.isAddDialogOpen.value = true
                },
                onDismiss = {
                    viewModel.selectedAnimeForDetail.value = null
                }
            )
        }
    }
}
