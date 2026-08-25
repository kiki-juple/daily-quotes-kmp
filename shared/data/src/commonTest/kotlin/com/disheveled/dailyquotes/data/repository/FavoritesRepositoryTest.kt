package com.disheveled.dailyquotes.data.repository

import com.disheveled.dailyquotes.data.api.ApiException
import com.disheveled.dailyquotes.data.api.FavQsApi
import com.disheveled.dailyquotes.data.api.SessionStore
import com.disheveled.dailyquotes.db.DailyQuotesDatabase
import com.disheveled.dailyquotes.domain.model.Quote
import com.russhwolf.settings.MapSettings
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FavoritesRepositoryTest {

    @Test
    fun refreshFollowsPaginationAndKeepsServerOrder() = runBlocking {
        val requestedPages = mutableListOf<String?>()
        val engine = MockEngine { request ->
            requestedPages += request.url.parameters["page"]
            when (request.url.parameters["page"]) {
                "1" -> respondJson(
                    quoteListJson(lastPage = false, quotes = listOf(1L to "Satu", 2L to "Dua")),
                )

                "2" -> respondJson(
                    quoteListJson(lastPage = true, quotes = listOf(3L to "Tiga")),
                )

                else -> error("unexpected page ${request.url.parameters["page"]}")
            }
        }
        val database = inMemoryDatabase()
        val repo = repository(engine, database, login = "kiki")

        repo.refreshFavorites()

        assertEquals(listOf<String?>("1", "2"), requestedPages)
        // selectAll orders by savedAtEpochMs DESC; refreshFavorites offsets each row by its index so
        // that ordering reproduces the order the server sent.
        assertEquals(
            listOf(1L, 2L, 3L),
            database.favoriteQuoteQueries.selectAll().executeAsList().map { it.id },
        )
    }

    @Test
    fun refreshStopsAtThePageCapWhenTheServerNeverSetsLastPage() = runBlocking {
        var pagesServed = 0
        val engine = MockEngine { request ->
            pagesServed += 1
            val page = request.url.parameters["page"]!!.toLong()
            // last_page is never true: without a cap this loop would never end.
            respondJson(quoteListJson(lastPage = false, quotes = listOf(page to "Halaman $page")))
        }
        val database = inMemoryDatabase()
        val repo = repository(engine, database, login = "kiki")

        repo.refreshFavorites()

        assertEquals(20, pagesServed, "pagination must give up at the cap")
        assertEquals(20, database.favoriteQuoteQueries.selectAll().executeAsList().size)
    }

    @Test
    fun refreshDropsLocalRowsTheServerNoLongerReturns() = runBlocking {
        val database = inMemoryDatabase()
        database.favoriteQuoteQueries.upsert(
            id = 999,
            body = "Sudah dihapus di server.",
            author = "Basi",
            favoritesCount = 0,
            savedAtEpochMs = 1,
        )
        val engine = MockEngine {
            respondJson(quoteListJson(lastPage = true, quotes = listOf(1L to "Masih ada")))
        }
        val repo = repository(engine, database, login = "kiki")

        repo.refreshFavorites()

        assertEquals(
            listOf(1L),
            database.favoriteQuoteQueries.selectAll().executeAsList().map { it.id },
        )
    }

    @Test
    fun refreshWithoutAStoredLoginIsANoOp() = runBlocking {
        val database = inMemoryDatabase()
        database.favoriteQuoteQueries.upsert(
            id = 5,
            body = "Tetap di sini.",
            author = "Lokal",
            favoritesCount = 0,
            savedAtEpochMs = 1,
        )
        val engine = MockEngine { error("no login means no request") }
        val repo = repository(engine, database, login = null)

        repo.refreshFavorites()

        assertEquals(1, database.favoriteQuoteQueries.selectAll().executeAsList().size)
    }

    @Test
    fun addStoresTheQuoteReturnedByTheApi() = runBlocking {
        val engine = MockEngine {
            respondJson("""{"id":42,"body":"Dari server.","author":"Server","favorites_count":9}""")
        }
        val database = inMemoryDatabase()
        val repo = repository(engine, database, login = "kiki")

        repo.add(Quote(id = 42, body = "Lokal.", author = "Lokal", favoritesCount = 0))

        val row = database.favoriteQuoteQueries.selectById(42).executeAsOne()
        assertEquals("Dari server.", row.body, "the API response wins over the local copy")
        assertEquals(9L, row.favoritesCount)
    }

    @Test
    fun addDoesNotTouchTheDatabaseWhenTheApiRejectsIt() = runBlocking {
        val engine = MockEngine {
            respondJson("""{"error_code":40,"message":"Quote not found."}""", HttpStatusCode.NotFound)
        }
        val database = inMemoryDatabase()
        val repo = repository(engine, database, login = "kiki")

        assertFailsWith<ApiException> {
            repo.add(Quote(id = 42, body = "Lokal.", author = "Lokal", favoritesCount = 0))
        }

        assertTrue(
            database.favoriteQuoteQueries.selectAll().executeAsList().isEmpty(),
            "a rejected favorite must not appear locally",
        )
    }

    @Test
    fun removeDeletesLocallyOnlyAfterTheApiAccepts() = runBlocking {
        val database = inMemoryDatabase()
        database.favoriteQuoteQueries.upsert(
            id = 7,
            body = "Akan dihapus.",
            author = "Anon",
            favoritesCount = 0,
            savedAtEpochMs = 1,
        )
        val engine = MockEngine {
            respondJson("""{"id":7,"body":"Akan dihapus.","author":"Anon","favorites_count":0}""")
        }
        val repo = repository(engine, database, login = "kiki")

        repo.remove(7)

        assertTrue(database.favoriteQuoteQueries.selectAll().executeAsList().isEmpty())
    }

    @Test
    fun removeKeepsTheRowWhenTheApiFails() = runBlocking {
        val database = inMemoryDatabase()
        database.favoriteQuoteQueries.upsert(
            id = 7,
            body = "Tetap ada.",
            author = "Anon",
            favoritesCount = 0,
            savedAtEpochMs = 1,
        )
        val engine = MockEngine {
            respondJson("""{"message":"Nope."}""", HttpStatusCode.InternalServerError)
        }
        val repo = repository(engine, database, login = "kiki")

        assertFailsWith<ApiException> { repo.remove(7) }

        assertEquals(1, database.favoriteQuoteQueries.selectAll().executeAsList().size)
    }

    @Test
    fun observeIsFavoriteTracksTheDatabase() = runBlocking {
        val database = inMemoryDatabase()
        val engine = MockEngine {
            respondJson("""{"id":8,"body":"Disimpan.","author":"Anon","favorites_count":1}""")
        }
        val repo = repository(engine, database, login = "kiki")

        assertFalse(withTimeout(5_000) { repo.observeIsFavorite(8).first() })

        repo.add(Quote(id = 8, body = "Disimpan.", author = "Anon", favoritesCount = 1))

        assertTrue(withTimeout(5_000) { repo.observeIsFavorite(8).first() })
    }

    @Test
    fun observeFavoritesEmitsTheStoredQuotes() = runBlocking {
        val database = inMemoryDatabase()
        database.favoriteQuoteQueries.upsert(
            id = 1,
            body = "Pertama.",
            author = "A",
            favoritesCount = 2,
            savedAtEpochMs = 10,
        )
        database.favoriteQuoteQueries.upsert(
            id = 2,
            body = "Kedua.",
            author = "B",
            favoritesCount = 3,
            savedAtEpochMs = 20,
        )
        val repo = repository(MockEngine { error("no request expected") }, database, login = "kiki")

        val quotes = withTimeout(5_000) { repo.observeFavorites().first() }

        assertEquals(listOf(2L, 1L), quotes.map { it.quote.id }, "newest saved first")
        assertEquals(3, quotes.first().quote.favoritesCount)
        assertEquals(20L, quotes.first().savedAtEpochMs, "the saved timestamp reaches the domain")
    }

    private fun repository(
        engine: MockEngine,
        database: DailyQuotesDatabase,
        login: String?,
    ) = DefaultFavoritesRepository(
        api = FavQsApi(buildTestClient(engine)),
        database = database,
        sessionStore = SessionStore(
            MapSettings().apply { login?.let { putString("login", it) } },
        ),
    )

    private fun quoteListJson(lastPage: Boolean, quotes: List<Pair<Long, String>>): String {
        val items = quotes.joinToString(",") { (id, body) ->
            """{"id":$id,"body":"$body","author":"Anon","favorites_count":1}"""
        }
        return """{"page":1,"last_page":$lastPage,"quotes":[$items]}"""
    }
}
