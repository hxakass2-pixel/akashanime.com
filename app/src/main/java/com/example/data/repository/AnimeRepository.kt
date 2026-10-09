package com.example.data.repository

import com.example.data.local.AnimeDao
import com.example.data.model.AnimeItem
import com.example.data.model.WatchStatus
import kotlinx.coroutines.flow.Flow

class AnimeRepository(private val animeDao: AnimeDao) {
    val allAnime: Flow<List<AnimeItem>> = animeDao.getAllAnime()
    val favoriteAnime: Flow<List<AnimeItem>> = animeDao.getFavoriteAnime()

    fun getAnimeByStatus(status: String): Flow<List<AnimeItem>> = animeDao.getAnimeByStatus(status)

    fun searchAnime(query: String): Flow<List<AnimeItem>> = animeDao.searchAnime(query)

    suspend fun getAnimeById(id: Long): AnimeItem? = animeDao.getAnimeById(id)

    suspend fun insertAnime(anime: AnimeItem): Long = animeDao.insert(anime)

    suspend fun updateAnime(anime: AnimeItem) = animeDao.update(anime)

    suspend fun deleteAnime(anime: AnimeItem) = animeDao.delete(anime)

    suspend fun deleteById(id: Long) = animeDao.deleteById(id)

    suspend fun incrementEpisode(anime: AnimeItem) {
        val nextEp = anime.episodesWatched + 1
        val updatedStatus = if (anime.totalEpisodes > 0 && nextEp >= anime.totalEpisodes) {
            WatchStatus.COMPLETED
        } else {
            WatchStatus.WATCHING
        }
        animeDao.update(
            anime.copy(
                episodesWatched = nextEp,
                status = updatedStatus
            )
        )
    }

    suspend fun toggleFavorite(anime: AnimeItem) {
        animeDao.update(anime.copy(isFavorite = !anime.isFavorite))
    }
}
