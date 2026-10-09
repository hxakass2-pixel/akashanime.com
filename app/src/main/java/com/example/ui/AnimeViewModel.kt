package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AnimeDatabase
import com.example.data.model.AnimeItem
import com.example.data.remote.AnimeAnalysisResult
import com.example.data.remote.AnimeAutoFillData
import com.example.data.remote.ChatMessage
import com.example.data.remote.GeminiService
import com.example.data.repository.AnimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AnimeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AnimeRepository
    private val geminiService = GeminiService()

    init {
        val db = AnimeDatabase.getDatabase(application)
        repository = AnimeRepository(db.animeDao())
    }

    // Filter & Search states
    val selectedFilter = MutableStateFlow("All")
    val searchQuery = MutableStateFlow("")

    val animeList: StateFlow<List<AnimeItem>> = combine(
        repository.allAnime,
        selectedFilter,
        searchQuery
    ) { list, filter, query ->
        var filtered = when (filter) {
            "All" -> list
            "Favorites" -> list.filter { it.isFavorite }
            else -> list.filter { it.status.equals(filter, ignoreCase = true) }
        }
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.japaneseTitle.contains(query, ignoreCase = true) ||
                it.genres.contains(query, ignoreCase = true) ||
                it.platform.contains(query, ignoreCase = true) ||
                it.notes.contains(query, ignoreCase = true)
            }
        }
        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dialog & selection states
    val isAddDialogOpen = MutableStateFlow(false)
    val selectedAnimeForDetail = MutableStateFlow<AnimeItem?>(null)
    val autoFillLoading = MutableStateFlow(false)
    val autoFillData = MutableStateFlow<AnimeAutoFillData?>(null)

    // Chatbot states
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                isUser = false,
                text = "Konnichiwa! I am your AKASH ANIME AI Sensei. " +
                       "Ask me anything about anime, recommendations, watch orders (Fate, Monogatari, etc.), " +
                       "episode counts, or finding legal links to stream any title!"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()
    val isChatLoading = MutableStateFlow(false)
    val selectedChatModel = MutableStateFlow(GeminiService.MODEL_FLASH)

    // Image analysis states
    val selectedImageBitmap = MutableStateFlow<Bitmap?>(null)
    val isAnalyzingImage = MutableStateFlow(false)
    val imageAnalysisResult = MutableStateFlow<AnimeAnalysisResult?>(null)
    val imageAnalysisError = MutableStateFlow<String?>(null)

    // Anime CRUD Actions
    fun addAnime(anime: AnimeItem) {
        viewModelScope.launch {
            repository.insertAnime(anime)
        }
    }

    fun updateAnime(anime: AnimeItem) {
        viewModelScope.launch {
            repository.updateAnime(anime)
        }
    }

    fun deleteAnime(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun incrementEpisode(anime: AnimeItem) {
        viewModelScope.launch {
            repository.incrementEpisode(anime)
        }
    }

    fun toggleFavorite(anime: AnimeItem) {
        viewModelScope.launch {
            repository.toggleFavorite(anime)
        }
    }

    fun autoFillFromLinkOrName(query: String, onComplete: (AnimeAutoFillData) -> Unit) {
        if (query.isBlank()) return
        autoFillLoading.value = true
        viewModelScope.launch {
            val result = geminiService.autoFillAnimeFromLinkOrName(query)
            autoFillLoading.value = false
            result.onSuccess { data ->
                autoFillData.value = data
                onComplete(data)
            }
        }
    }

    // Chat Actions
    fun sendChatMessage(text: String) {
        if (text.isBlank() || isChatLoading.value) return

        val userMsg = ChatMessage(isUser = true, text = text)
        _chatMessages.value = _chatMessages.value + userMsg
        isChatLoading.value = true

        viewModelScope.launch {
            val result = geminiService.sendChatMessage(
                history = _chatMessages.value.filter { !it.isError },
                newMessage = text,
                model = selectedChatModel.value
            )
            isChatLoading.value = false

            result.onSuccess { reply ->
                _chatMessages.value = _chatMessages.value + ChatMessage(isUser = false, text = reply)
            }.onFailure { err ->
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    isUser = false,
                    text = "Error: ${err.message ?: "Failed to generate reply"}",
                    isError = true
                )
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                isUser = false,
                text = "Chat cleared! How can I help you discover or track anime today?"
            )
        )
    }

    fun setChatModel(model: String) {
        selectedChatModel.value = model
    }

    // Image Understanding Actions (Using gemini-3.1-pro-preview)
    fun analyzeImage(bitmap: Bitmap, prompt: String = "") {
        selectedImageBitmap.value = bitmap
        isAnalyzingImage.value = true
        imageAnalysisError.value = null
        imageAnalysisResult.value = null

        viewModelScope.launch {
            val result = geminiService.analyzeAnimeImage(bitmap, prompt)
            isAnalyzingImage.value = false
            result.onSuccess { analysis ->
                imageAnalysisResult.value = analysis
            }.onFailure { err ->
                imageAnalysisError.value = err.message ?: "Failed to analyze image"
            }
        }
    }

    fun saveAnalyzedAnimeToLibrary(analysis: AnimeAnalysisResult, onSaved: () -> Unit) {
        viewModelScope.launch {
            val newAnime = AnimeItem(
                title = analysis.animeTitle,
                japaneseTitle = analysis.japaneseTitle,
                watchLink = analysis.suggestedLink.ifBlank { "https://www.crunchyroll.com" },
                status = com.example.data.model.WatchStatus.PLANNING,
                rating = 8.5f,
                synopsis = analysis.synopsis,
                episodesWatched = 0,
                totalEpisodes = 12,
                genres = analysis.genres,
                platform = analysis.watchPlatforms.firstOrNull() ?: "Crunchyroll",
                notes = "Identified via Gemini 3.1 Pro image analysis: Characters: ${analysis.characters.joinToString(", ")}",
                isFavorite = false
            )
            repository.insertAnime(newAnime)
            onSaved()
        }
    }
}
