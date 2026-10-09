package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database entity representing an Anime entry in the user's watch list.
 *
 * Core fields:
 * - [title]: Name of the anime
 * - [watchLink]: URL / link to watch or stream the anime
 * - [status]: Current watch progress ("watching", "completed", "planning")
 * - [rating]: User score / rating (e.g. 0.0 to 10.0)
 */
@Entity(tableName = "anime_watch_list")
data class AnimeItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "watch_link")
    val watchLink: String,

    @ColumnInfo(name = "status")
    val status: String = WatchStatus.WATCHING, // "watching", "completed", "planning"

    @ColumnInfo(name = "rating")
    val rating: Float = 0f,

    @ColumnInfo(name = "japanese_title")
    val japaneseTitle: String = "",

    @ColumnInfo(name = "image_url")
    val imageUrl: String = "",

    @ColumnInfo(name = "synopsis")
    val synopsis: String = "",

    @ColumnInfo(name = "episodes_watched")
    val episodesWatched: Int = 0,

    @ColumnInfo(name = "total_episodes")
    val totalEpisodes: Int = 0, // 0 indicates unknown or ongoing

    @ColumnInfo(name = "genres")
    val genres: String = "",

    @ColumnInfo(name = "platform")
    val platform: String = "Crunchyroll",

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    // Backwards-compatible aliases
    val url: String get() = watchLink
    val score: Float get() = rating
}

object WatchStatus {
    const val WATCHING = "watching"
    const val COMPLETED = "completed"
    const val PLANNING = "planning"

    val ALL = listOf(WATCHING, COMPLETED, PLANNING)
}
