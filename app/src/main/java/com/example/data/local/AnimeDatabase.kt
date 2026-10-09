package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AnimeItem
import com.example.data.model.WatchStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [AnimeItem::class], version = 2, exportSchema = false)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao

    companion object {
        @Volatile
        private var INSTANCE: AnimeDatabase? = null

        fun getDatabase(context: Context): AnimeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AnimeDatabase::class.java,
                    "akash_anime_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate initial anime starter list
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.animeDao()?.insertAll(getStarterAnimeList())
                            }
                        }
                    })
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getStarterAnimeList(): List<AnimeItem> = listOf(
            AnimeItem(
                title = "Naruto: Shippuden",
                japaneseTitle = "NARUTO -ナルト- 疾風伝",
                watchLink = "https://www.crunchyroll.com/series/GY9PJ5KWR/naruto-shippuden",
                status = WatchStatus.WATCHING,
                rating = 9.5f,
                imageUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
                synopsis = "Naruto Uzumaki returns to the Hidden Leaf Village older and stronger, fighting the Akatsuki to protect his comrades and bring Sasuke back.",
                episodesWatched = 380,
                totalEpisodes = 500,
                genres = "Action, Adventure, Shounen, Martial Arts",
                platform = "Crunchyroll",
                notes = "Believe it! Pain Arc and Fourth Great Ninja War are legendary.",
                isFavorite = true,
                createdAt = System.currentTimeMillis() - 86400000L * 4
            ),
            AnimeItem(
                title = "Frieren: Beyond Journey's End",
                japaneseTitle = "葬送のフリーレン (Sousou no Frieren)",
                watchLink = "https://www.crunchyroll.com/series/GG5H5XMQ5/frieren-beyond-journeys-end",
                status = WatchStatus.WATCHING,
                rating = 9.8f,
                imageUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80",
                synopsis = "Elf mage Frieren journeys through a world where peace has already been won, learning what humanity and memories truly mean.",
                episodesWatched = 20,
                totalEpisodes = 28,
                genres = "Fantasy, Adventure, Drama",
                platform = "Crunchyroll",
                notes = "Masterpiece storytelling, stunning OST, incredible animation.",
                isFavorite = true,
                createdAt = System.currentTimeMillis() - 86400000L * 3
            ),
            AnimeItem(
                title = "Jujutsu Kaisen Season 2",
                japaneseTitle = "呪術廻戦",
                watchLink = "https://www.crunchyroll.com/series/GRDV0019R/jujutsu-kaisen",
                status = WatchStatus.COMPLETED,
                rating = 9.4f,
                imageUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=600&auto=format&fit=crop&q=80",
                synopsis = "The Shibuya Incident arc unfolds as Gojo Satoru and sorcerers face catastrophic cursed spirit ambushes.",
                episodesWatched = 23,
                totalEpisodes = 23,
                genres = "Action, Supernatural, Fantasy",
                platform = "Crunchyroll",
                notes = "Shibuya Incident animation was unmatched hype!",
                isFavorite = true,
                createdAt = System.currentTimeMillis() - 86400000L * 2
            ),
            AnimeItem(
                title = "Solo Leveling",
                japaneseTitle = "俺だけレベルアップな件",
                watchLink = "https://www.crunchyroll.com/series/GDKH4EX4G/solo-leveling",
                status = WatchStatus.WATCHING,
                rating = 8.9f,
                imageUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=600&auto=format&fit=crop&q=80",
                synopsis = "Weakest E-rank hunter Sung Jinwoo discovers a mysterious quest window allowing him to level up endlessly.",
                episodesWatched = 8,
                totalEpisodes = 12,
                genres = "Action, Fantasy, Adventure",
                platform = "Crunchyroll",
                notes = "Arise! Super satisfying progression.",
                isFavorite = false,
                createdAt = System.currentTimeMillis() - 86400000L
            ),
            AnimeItem(
                title = "Demon Slayer: Hashira Training Arc",
                japaneseTitle = "鬼滅の刃 柱稽古編",
                watchLink = "https://www.crunchyroll.com/series/GYXNW2VP6/demon-slayer-kimetsu-no-yaiba",
                status = WatchStatus.PLANNING,
                rating = 8.7f,
                imageUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
                synopsis = "Tanjiro visits Stone Hashira Himejima to undergo intense preparation for the looming final battle against Muzan.",
                episodesWatched = 0,
                totalEpisodes = 8,
                genres = "Action, Historical, Shounen",
                platform = "Crunchyroll",
                notes = "Preparing for Infinity Castle movies!",
                isFavorite = false,
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
