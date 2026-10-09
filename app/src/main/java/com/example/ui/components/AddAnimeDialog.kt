package com.example.ui.components

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AnimeItem
import com.example.data.model.WatchStatus
import com.example.ui.AnimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnimeDialog(
    viewModel: AnimeViewModel,
    initialAnime: AnimeItem? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isAutoFilling by viewModel.autoFillLoading.collectAsState()

    var title by remember { mutableStateOf(initialAnime?.title ?: "") }
    var japaneseTitle by remember { mutableStateOf(initialAnime?.japaneseTitle ?: "") }
    var url by remember { mutableStateOf(initialAnime?.watchLink ?: "") }
    var imageUrl by remember { mutableStateOf(initialAnime?.imageUrl ?: "") }
    var synopsis by remember { mutableStateOf(initialAnime?.synopsis ?: "") }
    var status by remember { mutableStateOf(initialAnime?.status ?: WatchStatus.WATCHING) }
    var episodesWatched by remember { mutableStateOf(initialAnime?.episodesWatched?.toString() ?: "0") }
    var totalEpisodes by remember { mutableStateOf(initialAnime?.totalEpisodes?.toString() ?: "12") }
    var score by remember { mutableStateOf(initialAnime?.rating?.toString() ?: "8.5") }
    var genres by remember { mutableStateOf(initialAnime?.genres ?: "Action, Fantasy") }
    var platform by remember { mutableStateOf(initialAnime?.platform ?: "Crunchyroll") }
    var notes by remember { mutableStateOf(initialAnime?.notes ?: "") }

    val statusOptions = listOf(WatchStatus.WATCHING, WatchStatus.COMPLETED, WatchStatus.PLANNING)
    val platformOptions = listOf("Crunchyroll", "Netflix", "HiDive", "AniList", "MyAnimeList", "YouTube", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .testTag("add_anime_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialAnime == null) "Add Anime Link" else "Edit Anime",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable Form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Link Input with Paste button
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("Anime Link / URL") },
                        placeholder = { Text("https://www.crunchyroll.com/...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("anime_url_input"),
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                if (!clip.isNullOrBlank()) {
                                    url = clip
                                }
                            }) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Paste Link")
                            }
                        },
                        singleLine = true
                    )

                    // Auto-Fill Button using Gemini
                    Button(
                        onClick = {
                            val query = url.ifBlank { title }
                            if (query.isNotBlank()) {
                                viewModel.autoFillFromLinkOrName(query) { data ->
                                    if (data.title.isNotBlank()) title = data.title
                                    if (data.japaneseTitle.isNotBlank()) japaneseTitle = data.japaneseTitle
                                    if (data.synopsis.isNotBlank()) synopsis = data.synopsis
                                    if (data.totalEpisodes > 0) totalEpisodes = data.totalEpisodes.toString()
                                    if (data.genres.isNotBlank()) genres = data.genres
                                    if (data.platform.isNotBlank()) platform = data.platform
                                    if (data.suggestedLink.isNotBlank() && url.isBlank()) url = data.suggestedLink
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_autofill_button"),
                        enabled = !isAutoFilling && (url.isNotBlank() || title.isNotBlank()),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        if (isAutoFilling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Fetching Details...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Auto-Fill Details with AI")
                        }
                    }

                    // Anime Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Anime Title *") },
                        placeholder = { Text("e.g. Frieren, Solo Leveling") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("anime_title_input"),
                        leadingIcon = { Icon(Icons.Default.Movie, contentDescription = null) },
                        singleLine = true
                    )

                    // Japanese Title
                    OutlinedTextField(
                        value = japaneseTitle,
                        onValueChange = { japaneseTitle = it },
                        label = { Text("Japanese / Romaji Title") },
                        placeholder = { Text("e.g. 葬送のフリーレン") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Platform Selection Chips
                    Text(
                        text = "Streaming Platform",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        platformOptions.take(4).forEach { p ->
                            FilterChip(
                                selected = platform == p,
                                onClick = { platform = p },
                                label = { Text(p, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }

                    // Status Selection Chips
                    Text(
                        text = "Watch Status",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        statusOptions.take(3).forEach { s ->
                            FilterChip(
                                selected = status == s,
                                onClick = { status = s },
                                label = { Text(s, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }

                    // Episodes Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = episodesWatched,
                            onValueChange = { episodesWatched = it.filter { c -> c.isDigit() } },
                            label = { Text("Watched Ep") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = totalEpisodes,
                            onValueChange = { totalEpisodes = it.filter { c -> c.isDigit() } },
                            label = { Text("Total Ep") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Rating & Genres
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = score,
                            onValueChange = { score = it },
                            label = { Text("Score (0-10)") },
                            modifier = Modifier.weight(1f),
                            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = genres,
                            onValueChange = { genres = it },
                            label = { Text("Genres") },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true
                        )
                    }

                    // Poster Image URL
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Poster Image URL (Optional)") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) },
                        singleLine = true
                    )

                    // Synopsis
                    OutlinedTextField(
                        value = synopsis,
                        onValueChange = { synopsis = it },
                        label = { Text("Synopsis / Plot") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    // Personal Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Personal Notes & Review") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val item = AnimeItem(
                                    id = initialAnime?.id ?: 0,
                                    title = title.trim(),
                                    japaneseTitle = japaneseTitle.trim(),
                                    watchLink = url.trim().ifBlank { "https://www.crunchyroll.com" },
                                    status = status.lowercase(),
                                    rating = score.toFloatOrNull() ?: 0f,
                                    imageUrl = imageUrl.trim(),
                                    synopsis = synopsis.trim(),
                                    episodesWatched = episodesWatched.toIntOrNull() ?: 0,
                                    totalEpisodes = totalEpisodes.toIntOrNull() ?: 0,
                                    genres = genres.trim(),
                                    platform = platform,
                                    notes = notes.trim(),
                                    isFavorite = initialAnime?.isFavorite ?: false,
                                    createdAt = initialAnime?.createdAt ?: System.currentTimeMillis()
                                )
                                if (initialAnime == null) {
                                    viewModel.addAnime(item)
                                } else {
                                    viewModel.updateAnime(item)
                                }
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_anime_button"),
                        enabled = title.isNotBlank()
                    ) {
                        Text(if (initialAnime == null) "Add Anime" else "Save Changes")
                    }
                }
            }
        }
    }
}
