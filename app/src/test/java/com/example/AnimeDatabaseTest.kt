package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AnimeDao
import com.example.data.local.AnimeDatabase
import com.example.data.model.AnimeItem
import com.example.data.model.WatchStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AnimeDatabaseTest {

    private lateinit var db: AnimeDatabase
    private lateinit var animeDao: AnimeDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AnimeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        animeDao = db.animeDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndQueryAnimeWatchList() = runBlocking {
        val naruto = AnimeItem(
            title = "Naruto: Shippuden",
            watchLink = "https://www.crunchyroll.com/series/GY9PJ5KWR/naruto-shippuden",
            status = WatchStatus.WATCHING,
            rating = 9.5f,
            japaneseTitle = "NARUTO -ナルト- 疾風伝",
            episodesWatched = 380,
            totalEpisodes = 500
        )

        val id = animeDao.insert(naruto)
        assertTrue(id > 0)

        val retrieved = animeDao.getAnimeById(id)
        assertNotNull(retrieved)
        assertEquals("Naruto: Shippuden", retrieved?.title)
        assertEquals("https://www.crunchyroll.com/series/GY9PJ5KWR/naruto-shippuden", retrieved?.watchLink)
        assertEquals("watching", retrieved?.status)
        assertEquals(9.5f, retrieved?.rating)
    }

    @Test
    fun queryByStatusWatchingCompletedPlanning() = runBlocking {
        animeDao.insert(
            AnimeItem(
                title = "Naruto",
                watchLink = "https://crunchyroll.com/naruto",
                status = WatchStatus.WATCHING,
                rating = 9.0f
            )
        )
        animeDao.insert(
            AnimeItem(
                title = "Jujutsu Kaisen",
                watchLink = "https://crunchyroll.com/jjk",
                status = WatchStatus.COMPLETED,
                rating = 9.4f
            )
        )
        animeDao.insert(
            AnimeItem(
                title = "Demon Slayer",
                watchLink = "https://crunchyroll.com/demon-slayer",
                status = WatchStatus.PLANNING,
                rating = 8.8f
            )
        )

        val watchingList = animeDao.getAnimeByStatus("watching").first()
        assertEquals(1, watchingList.size)
        assertEquals("Naruto", watchingList[0].title)

        val completedList = animeDao.getAnimeByStatus("completed").first()
        assertEquals(1, completedList.size)
        assertEquals("Jujutsu Kaisen", completedList[0].title)

        val planningList = animeDao.getAnimeByStatus("planning").first()
        assertEquals(1, planningList.size)
        assertEquals("Demon Slayer", planningList[0].title)
    }

    @Test
    fun updateStatusAndRating() = runBlocking {
        val item = AnimeItem(
            title = "Solo Leveling",
            watchLink = "https://crunchyroll.com/solo-leveling",
            status = WatchStatus.WATCHING,
            rating = 8.5f
        )
        val id = animeDao.insert(item)
        val inserted = animeDao.getAnimeById(id)!!

        val updated = inserted.copy(
            status = WatchStatus.COMPLETED,
            rating = 9.8f
        )
        animeDao.update(updated)

        val reloaded = animeDao.getAnimeById(id)!!
        assertEquals(WatchStatus.COMPLETED, reloaded.status)
        assertEquals(9.8f, reloaded.rating)
    }

    @Test
    fun deleteFromWatchList() = runBlocking {
        val item = AnimeItem(
            title = "Frieren",
            watchLink = "https://crunchyroll.com/frieren",
            status = WatchStatus.WATCHING,
            rating = 9.9f
        )
        val id = animeDao.insert(item)
        assertEquals(1, animeDao.getCount())

        animeDao.deleteById(id)
        assertEquals(0, animeDao.getCount())
    }
}
