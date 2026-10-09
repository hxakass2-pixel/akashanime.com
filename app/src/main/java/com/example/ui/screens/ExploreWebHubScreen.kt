package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AnimeItem
import com.example.ui.AnimeViewModel

data class WebPlatformLink(
    val name: String,
    val url: String,
    val description: String,
    val tag: String
)

data class FeaturedAnime(
    val title: String,
    val japaneseTitle: String,
    val url: String,
    val imageUrl: String,
    val synopsis: String,
    val genres: String,
    val platform: String,
    val totalEp: Int,
    val score: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreWebHubScreen(
    viewModel: AnimeViewModel,
    onOpenWebBrowser: (String) -> Unit,
    onAddAnimeWithLink: (url: String, title: String) -> Unit
) {
    val context = LocalContext.current

    val platforms = listOf(
        WebPlatformLink(
            name = "Crunchyroll",
            url = "https://www.crunchyroll.com",
            description = "World's largest legal anime streaming library with simulcasts.",
            tag = "Streaming"
        ),
        WebPlatformLink(
            name = "AniList",
            url = "https://anilist.co",
            description = "Modern anime and manga tracking platform with community reviews.",
            tag = "Database"
        ),
        WebPlatformLink(
            name = "MyAnimeList",
            url = "https://myanimelist.net",
            description = "Comprehensive global anime catalog, rankings, and discussions.",
            tag = "Catalog"
        ),
        WebPlatformLink(
            name = "HiDive",
            url = "https://www.hidive.com",
            description = "Exclusive anime streaming, classic series, and uncensored dubs.",
            tag = "Streaming"
        ),
        WebPlatformLink(
            name = "AnimeNewsNetwork",
            url = "https://www.animenewsnetwork.com",
            description = "Breaking news, industry interviews, and seasonal anime release guides.",
            tag = "News"
        ),
        WebPlatformLink(
            name = "Netflix Anime",
            url = "https://www.netflix.com/browse/genre/7424",
            description = "Original anime productions and globally popular licensed titles.",
            tag = "Streaming"
        )
    )

    val featuredAnimeList = listOf(
        FeaturedAnime(
            title = "Attack on Titan: Final Season",
            japaneseTitle = "進撃の巨人 The Final Season",
            url = "https://www.crunchyroll.com/series/GR751KNZY/attack-on-titan",
            imageUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80",
            synopsis = "The battle for Paradis reaches its world-shattering conclusion as Eren Yeager initiates the Rumbling.",
            genres = "Action, Military, Mystery, Drama",
            platform = "Crunchyroll",
            totalEp = 28,
            score = 9.7f
        ),
        FeaturedAnime(
            title = "Chainsaw Man",
            japaneseTitle = "チェンソーマン",
            url = "https://www.crunchyroll.com/series/GVDHX8QNW/chainsaw-man",
            imageUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=600&auto=format&fit=crop&q=80",
            synopsis = "Denji merges with his devil-dog Pochita to become Chainsaw Man, joining the Public Safety Devil Hunters.",
            genres = "Action, Supernatural, Gore",
            platform = "Crunchyroll",
            totalEp = 12,
            score = 8.8f
        ),
        FeaturedAnime(
            title = "Spy x Family Season 2",
            japaneseTitle = "SPY×FAMILY",
            url = "https://www.crunchyroll.com/series/G4PH0WVPJ/spy-x-family",
            imageUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=600&auto=format&fit=crop&q=80",
            synopsis = "Twilight, Yor, and telepathic Anya continue their hilarious secret double lives in the Forger family.",
            genres = "Comedy, Action, Slice of Life",
            platform = "Crunchyroll",
            totalEp = 25,
            score = 8.9f
        ),
        FeaturedAnime(
            title = "Bleach: Thousand-Year Blood War",
            japaneseTitle = "BLEACH 千年血戦篇",
            url = "https://www.hulu.com/series/bleach-thousand-year-blood-war",
            imageUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
            synopsis = "The Soul Society faces extinction as the Quincy King Yhwach declares all-out war.",
            genres = "Action, Supernatural, Shounen",
            platform = "Hulu",
            totalEp = 26,
            score = 9.1f
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_naruto_logo),
                            contentDescription = "Naruto Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "akashanime.com",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Web Portal & Streaming Links Hub",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("explore_web_hub_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Web Portal Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_naruto_logo),
                                contentDescription = "Naruto Logo",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Welcome to akashanime.com",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Your unified anime web portal. Add and stream any anime link, use our built-in web player, analyze screenshots with Gemini 3.1 Pro, and chat with Otaku Sensei AI.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onOpenWebBrowser("https://www.crunchyroll.com") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Launch In-App Web Browser")
                        }
                    }
                }
            }

            // Top Streaming & Database Platforms
            item {
                Text(
                    text = "Official Anime Streaming Platforms",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(platforms) { platform ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenWebBrowser(platform.url) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = platform.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = platform.tag,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = platform.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { onOpenWebBrowser(platform.url) }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Featured & Trending Anime with 1-Click Save
            item {
                Text(
                    text = "Trending on akashanime.com (1-Click Save)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(featuredAnimeList) { anime ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 64.dp, height = 80.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                AsyncImage(
                                    model = anime.imageUrl,
                                    contentDescription = anime.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = anime.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = anime.japaneseTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "★ ${anime.score} • ${anime.platform} • ${anime.totalEp} Ep",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = anime.synopsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.addAnime(
                                        AnimeItem(
                                            title = anime.title,
                                            japaneseTitle = anime.japaneseTitle,
                                            watchLink = anime.url,
                                            imageUrl = anime.imageUrl,
                                            synopsis = anime.synopsis,
                                            status = com.example.data.model.WatchStatus.PLANNING,
                                            episodesWatched = 0,
                                            totalEpisodes = anime.totalEp,
                                            rating = anime.score,
                                            genres = anime.genres,
                                            platform = anime.platform,
                                            isFavorite = false
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Link")
                            }

                            OutlinedButton(
                                onClick = { onOpenWebBrowser(anime.url) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Web Watch")
                            }
                        }
                    }
                }
            }
        }
    }
}
