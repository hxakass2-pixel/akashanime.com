package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

data class AnimeAnalysisResult(
    val animeTitle: String,
    val japaneseTitle: String,
    val characters: List<String>,
    val sceneDescription: String,
    val synopsis: String,
    val genres: String,
    val watchPlatforms: List<String>,
    val suggestedLink: String,
    val rawText: String
)

data class AnimeAutoFillData(
    val title: String,
    val japaneseTitle: String,
    val synopsis: String,
    val totalEpisodes: Int,
    val genres: String,
    val platform: String,
    val suggestedLink: String
)

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        const val MODEL_PRO = "gemini-3.1-pro-preview"
        const val MODEL_FLASH = "gemini-3.5-flash"
        const val MODEL_FLASH_LITE = "gemini-3.1-flash-lite-preview"

        const val SYSTEM_INSTRUCTION_CHATBOT =
            "You are AKASH ANIME AI Assistant & Otaku Sensei. You are an expert, friendly anime guide and companion. " +
            "You help users organize anime watchlists, discover legal anime streaming links (Crunchyroll, Netflix, HiDive, AniList, MAL), " +
            "explain complex plots and watch orders (e.g. Fate, Monogatari), discuss character lore, and recommend series. " +
            "Be enthusiastic, knowledgeable, and organize your responses with clean markdown and bullet points."
    }

    private fun getApiKey(): String {
        return BuildConfig.GEMINI_API_KEY.trim()
    }

    /**
     * Multi-turn chat conversation using conversation history
     */
    suspend fun sendChatMessage(
        history: List<ChatMessage>,
        newMessage: String,
        model: String = MODEL_FLASH
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                Exception("Gemini API key is not configured. Please set GEMINI_API_KEY in the Secrets panel in AI Studio.")
            )
        }

        try {
            val root = JSONObject()

            // System instruction
            val systemInstruction = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", SYSTEM_INSTRUCTION_CHATBOT))
            systemInstruction.put("parts", sysParts)
            root.put("systemInstruction", systemInstruction)

            // Contents array (Conversation history + new message)
            val contents = JSONArray()
            for (msg in history.takeLast(16)) { // keep reasonable context window
                val contentObj = JSONObject()
                contentObj.put("role", if (msg.isUser) "user" else "model")
                val parts = JSONArray()
                parts.put(JSONObject().put("text", msg.text))
                contentObj.put("parts", parts)
                contents.put(contentObj)
            }

            // Append the new user turn
            val userContent = JSONObject()
            userContent.put("role", "user")
            val userParts = JSONArray()
            userParts.put(JSONObject().put("text", newMessage))
            userContent.put("parts", userParts)
            contents.put(userContent)

            root.put("contents", contents)

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            root.put("generationConfig", genConfig)

            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            val body = root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(endpoint).post(body).build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    errJson.optJSONObject("error")?.optString("message") ?: "HTTP error ${response.code}"
                } catch (e: Exception) {
                    "HTTP error ${response.code}: $responseBody"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (text != null && text.isNotEmpty()) {
                Result.success(text)
            } else {
                Result.failure(Exception("Empty response received from Gemini."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Analyze image using gemini-3.1-pro-preview as specified by user
     */
    suspend fun analyzeAnimeImage(
        bitmap: Bitmap,
        customPrompt: String = ""
    ): Result<AnimeAnalysisResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                Exception("Gemini API key is not configured. Please set GEMINI_API_KEY in the Secrets panel in AI Studio.")
            )
        }

        try {
            // Compress bitmap to JPEG Base64
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val root = JSONObject()

            // System instruction
            val systemInstruction = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(
                JSONObject().put(
                    "text",
                    "You are an expert anime visual recognition and analysis system. " +
                    "Analyze anime screenshots, posters, and manga panels with high precision. " +
                    "Identify the anime title (English and Japanese Romaji/Kanji), recognizable characters, the specific scene or context, " +
                    "genres, plot summary, and legal streaming links or platforms where users can watch it."
                )
            )
            systemInstruction.put("parts", sysParts)
            root.put("systemInstruction", systemInstruction)

            val promptText = if (customPrompt.isNotBlank()) {
                customPrompt + "\n\nProvide your analysis with structured sections: " +
                "1. ANIME TITLE\n2. JAPANESE TITLE\n3. IDENTIFIED CHARACTERS\n4. SCENE CONTEXT\n5. SYNOPSIS & GENRES\n6. WHERE TO WATCH & LINKS"
            } else {
                "Analyze this anime image in detail: identify the anime title, Japanese title, characters present in this shot, " +
                "scene/arc context, synopsis, genres, and recommended legal streaming platforms and links to watch it. " +
                "Format cleanly with headers."
            }

            val contents = JSONArray()
            val content = JSONObject()
            content.put("role", "user")

            val parts = JSONArray()
            parts.put(JSONObject().put("text", promptText))

            val inlineData = JSONObject()
            inlineData.put("mimeType", "image/jpeg")
            inlineData.put("data", base64Image)
            parts.put(JSONObject().put("inlineData", inlineData))

            content.put("parts", parts)
            contents.put(content)
            root.put("contents", contents)

            // Strictly use gemini-3.1-pro-preview for image understanding
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_PRO:generateContent?key=$apiKey"
            val body = root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(endpoint).post(body).build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    errJson.optJSONObject("error")?.optString("message") ?: "HTTP error ${response.code}"
                } catch (e: Exception) {
                    "HTTP error ${response.code}: $responseBody"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val responseContent = firstCandidate?.optJSONObject("content")
            val resParts = responseContent?.optJSONArray("parts")
            val fullText = resParts?.optJSONObject(0)?.optString("text") ?: ""

            if (fullText.isEmpty()) {
                return@withContext Result.failure(Exception("Unable to analyze image."))
            }

            // Parse structured details from response
            val parsedResult = parseAnalysisText(fullText)
            Result.success(parsedResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * AI-Powered Auto-Fill: Inspects a link or title to extract complete anime metadata
     */
    suspend fun autoFillAnimeFromLinkOrName(
        input: String
    ): Result<AnimeAutoFillData> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Provide intelligent local fallback extraction from URL
            return@withContext Result.success(extractFallbackDataFromInput(input))
        }

        try {
            val root = JSONObject()
            val prompt = """
                The user wants to add an anime to their library from this link or text: "$input".
                Identify the anime and return ONLY a valid JSON object with the following fields:
                {
                  "title": "Full English / Primary Title",
                  "japaneseTitle": "Japanese Kanji and Romaji",
                  "synopsis": "Brief engaging synopsis of 2-3 sentences",
                  "totalEpisodes": integer (or 0 if unknown/ongoing),
                  "genres": "Comma separated genres e.g. Action, Fantasy, Shounen",
                  "platform": "Primary platform e.g. Crunchyroll, Netflix, HiDive, AniList, YouTube, etc.",
                  "suggestedLink": "Direct clean link to watch or view this anime"
                }
                Return ONLY the JSON string. Do not wrap in markdown or backticks.
            """.trimIndent()

            val contents = JSONArray()
            val content = JSONObject()
            val parts = JSONArray()
            parts.put(JSONObject().put("text", prompt))
            content.put("parts", parts)
            contents.put(content)
            root.put("contents", contents)

            // Fast model for quick metadata retrieval
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_FLASH_LITE:generateContent?key=$apiKey"
            val body = root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(endpoint).post(body).build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.success(extractFallbackDataFromInput(input))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val text = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text") ?: ""

            val cleanedJson = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val dataObj = JSONObject(cleanedJson)

            val autoFill = AnimeAutoFillData(
                title = dataObj.optString("title").ifBlank { input },
                japaneseTitle = dataObj.optString("japaneseTitle"),
                synopsis = dataObj.optString("synopsis"),
                totalEpisodes = dataObj.optInt("totalEpisodes", 12),
                genres = dataObj.optString("genres"),
                platform = dataObj.optString("platform").ifBlank { detectPlatform(input) },
                suggestedLink = dataObj.optString("suggestedLink").ifBlank { input }
            )
            Result.success(autoFill)
        } catch (e: Exception) {
            // Graceful fallback to regex parsing
            Result.success(extractFallbackDataFromInput(input))
        }
    }

    private fun detectPlatform(url: String): String {
        val lower = url.lowercase()
        return when {
            lower.contains("crunchyroll") -> "Crunchyroll"
            lower.contains("netflix") -> "Netflix"
            lower.contains("hidive") -> "HiDive"
            lower.contains("anilist") -> "AniList"
            lower.contains("myanimelist") -> "MyAnimeList"
            lower.contains("youtube") || lower.contains("youtu.be") -> "YouTube"
            lower.contains("bilibili") -> "Bilibili"
            lower.contains("hulu") -> "Hulu"
            else -> "Web Link"
        }
    }

    private fun extractFallbackDataFromInput(input: String): AnimeAutoFillData {
        val platform = detectPlatform(input)
        val cleanTitle = if (input.startsWith("http://", ignoreCase = true) || input.startsWith("https://", ignoreCase = true)) {
            // Derive title from URL path
            val path = input.substringAfterLast("/").substringBefore("?").replace("-", " ").replace("_", " ")
            if (path.length > 2) path.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } } else "New Anime"
        } else {
            input
        }

        return AnimeAutoFillData(
            title = cleanTitle,
            japaneseTitle = "",
            synopsis = "Added from $platform",
            totalEpisodes = 12,
            genres = "Anime, Animation",
            platform = platform,
            suggestedLink = input
        )
    }

    private fun parseAnalysisText(text: String): AnimeAnalysisResult {
        var animeTitle = "Identified Anime"
        var japaneseTitle = ""
        val characters = mutableListOf<String>()
        var sceneDesc = ""
        var synopsis = ""
        var genres = "Action, Fantasy"
        val platforms = mutableListOf<String>()
        var suggestedLink = "https://www.crunchyroll.com"

        val lines = text.lines()
        for (line in lines) {
            val trimmed = line.trim()
            val lower = trimmed.lowercase()
            when {
                lower.startsWith("1. anime title") || lower.startsWith("anime title:") -> {
                    animeTitle = trimmed.substringAfter(":").ifBlank { trimmed.substringAfter("-") }.trim().trim('*')
                }
                lower.startsWith("2. japanese title") || lower.startsWith("japanese title:") -> {
                    japaneseTitle = trimmed.substringAfter(":").ifBlank { trimmed.substringAfter("-") }.trim().trim('*')
                }
                lower.startsWith("character") || lower.startsWith("- character") || lower.startsWith("* character") -> {
                    val charName = trimmed.substringAfter(":").trim().trim('*')
                    if (charName.isNotBlank() && characters.size < 5) characters.add(charName)
                }
                lower.contains("crunchyroll") -> {
                    if (!platforms.contains("Crunchyroll")) platforms.add("Crunchyroll")
                    suggestedLink = "https://www.crunchyroll.com/search?q=" + java.net.URLEncoder.encode(animeTitle, "UTF-8")
                }
                lower.contains("netflix") -> {
                    if (!platforms.contains("Netflix")) platforms.add("Netflix")
                }
                lower.contains("hidive") -> {
                    if (!platforms.contains("HiDive")) platforms.add("HiDive")
                }
            }
        }

        if (platforms.isEmpty()) {
            platforms.add("Crunchyroll")
            platforms.add("AniList")
        }

        // If title still looks generic, pick first heading
        if (animeTitle == "Identified Anime" || animeTitle.isBlank()) {
            for (line in lines) {
                if (line.startsWith("#") || line.contains("**")) {
                    val candidate = line.replace("#", "").replace("*", "").trim()
                    if (candidate.length in 3..60 && !candidate.contains("analysis", ignoreCase = true)) {
                        animeTitle = candidate
                        break
                    }
                }
            }
        }

        return AnimeAnalysisResult(
            animeTitle = animeTitle.ifBlank { "Identified Anime" },
            japaneseTitle = japaneseTitle,
            characters = if (characters.isNotEmpty()) characters else listOf("Main Protagonist", "Key Cast"),
            sceneDescription = sceneDesc,
            synopsis = synopsis.ifBlank { "Exciting anime series featuring high-stakes action and compelling character journeys." },
            genres = genres,
            watchPlatforms = platforms,
            suggestedLink = suggestedLink,
            rawText = text
        )
    }
}
