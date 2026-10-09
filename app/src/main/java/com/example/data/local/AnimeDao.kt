package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AnimeItem
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the anime watch list table.
 */
@Dao
interface AnimeDao {
    @Query("SELECT * FROM anime_watch_list ORDER BY created_at DESC")
    fun getAllAnime(): Flow<List<AnimeItem>>

    @Query("SELECT * FROM anime_watch_list WHERE id = :id")
    suspend fun getAnimeById(id: Long): AnimeItem?

    @Query("SELECT * FROM anime_watch_list WHERE LOWER(status) = LOWER(:status) ORDER BY created_at DESC")
    fun getAnimeByStatus(status: String): Flow<List<AnimeItem>>

    @Query("SELECT * FROM anime_watch_list WHERE is_favorite = 1 ORDER BY created_at DESC")
    fun getFavoriteAnime(): Flow<List<AnimeItem>>

    @Query("SELECT * FROM anime_watch_list WHERE title LIKE '%' || :query || '%' OR synopsis LIKE '%' || :query || '%' OR genres LIKE '%' || :query || '%' ORDER BY created_at DESC")
    fun searchAnime(query: String): Flow<List<AnimeItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(anime: AnimeItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(animeList: List<AnimeItem>)

    @Update
    suspend fun update(anime: AnimeItem)

    @Delete
    suspend fun delete(anime: AnimeItem)

    @Query("DELETE FROM anime_watch_list WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM anime_watch_list")
    suspend fun getCount(): Int
}
