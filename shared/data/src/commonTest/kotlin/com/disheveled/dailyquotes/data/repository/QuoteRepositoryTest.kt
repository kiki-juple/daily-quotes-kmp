package com.disheveled.dailyquotes.data.repository

import com.disheveled.dailyquotes.data.api.FavQsApi
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class QuoteRepositoryTest {

    @Test
    fun cacheMissFetchesFromApiAndCachesUnderTodaysDate() = runTest {
        var calls = 0
        val engine = MockEngine {
            calls += 1
            respondJson(qotdJson(id = 11, body = "Mulai dari yang kecil.", author = "Renung"))
        }
        val database = inMemoryDatabase()
        val repo = repository(engine, database, at = "2026-08-25T09:00:00Z")

        val result = repo.getQuoteOfTheDay()

        assertTrue(result.isSuccess, "expected success but got ${result.exceptionOrNull()}")
        assertEquals(11L, result.getOrThrow().id)
        assertEquals(1, calls)

        val cached = database.quoteOfTheDayQueries.selectForDate("2026-08-25").executeAsOneOrNull()
        assertEquals(11L, cached?.id)
        assertEquals("Renung", cached?.author)
    }

    @Test
    fun cacheHitForTodayIsServedWithoutCallingTheApi() = runTest {
        val database = inMemoryDatabase()
        database.quoteOfTheDayQueries.upsert(
            qotdDate = "2026-08-25",
            id = 77,
            body = "Dari cache.",
            author = "Lokal",
            favoritesCount = 5,
        )
        val engine = MockEngine { error("the API must not be called while today's quote is cached") }
        val repo = repository(engine, database, at = "2026-08-25T23:59:00Z")

        val result = repo.getQuoteOfTheDay()

        assertTrue(result.isSuccess, "expected success but got ${result.exceptionOrNull()}")
        val quote = result.getOrThrow()
        assertEquals(77L, quote.id)
        assertEquals("Dari cache.", quote.body)
        assertEquals(5, quote.favoritesCount)
    }

    @Test
    fun crossingMidnightRefetchesAndEvictsThePreviousDay() = runTest {
        val database = inMemoryDatabase()
        database.quoteOfTheDayQueries.upsert(
            qotdDate = "2026-08-24",
            id = 1,
            body = "Kutipan kemarin.",
            author = "Kemarin",
            favoritesCount = 0,
        )
        val engine = MockEngine { respondJson(qotdJson(id = 2, body = "Kutipan hari ini.")) }
        val repo = repository(engine, database, at = "2026-08-25T00:05:00Z")

        val result = repo.getQuoteOfTheDay()

        assertEquals(2L, result.getOrThrow().id)
        assertNull(
            database.quoteOfTheDayQueries.selectForDate("2026-08-24").executeAsOneOrNull(),
            "yesterday's cache must be evicted rather than piling up",
        )
        assertEquals(2L, database.quoteOfTheDayQueries.selectForDate("2026-08-25").executeAsOneOrNull()?.id)
    }

    @Test
    fun theLocalTimeZoneDecidesWhichDayIsCached() = runTest {
        // 23:30 UTC on the 24th is already 06:30 on the 25th in Jakarta (UTC+7).
        val database = inMemoryDatabase()
        val engine = MockEngine { respondJson(qotdJson(id = 3, body = "Pagi Jakarta.")) }
        val repo = repository(
            engine,
            database,
            at = "2026-08-24T23:30:00Z",
            timeZone = TimeZone.of("Asia/Jakarta"),
        )

        repo.getQuoteOfTheDay()

        assertEquals(
            3L,
            database.quoteOfTheDayQueries.selectForDate("2026-08-25").executeAsOneOrNull()?.id,
            "the cache key must follow the device's calendar day, not UTC",
        )
    }

    @Test
    fun apiFailureIsReportedAndNothingIsCached() = runTest {
        val database = inMemoryDatabase()
        val engine = MockEngine {
            respondJson("""{"error_code":50,"message":"Boom."}""", HttpStatusCode.InternalServerError)
        }
        val repo = repository(engine, database, at = "2026-08-25T09:00:00Z")

        val result = repo.getQuoteOfTheDay()

        assertTrue(result.isFailure)
        assertNull(database.quoteOfTheDayQueries.selectForDate("2026-08-25").executeAsOneOrNull())
    }

    private fun repository(
        engine: MockEngine,
        database: com.disheveled.dailyquotes.db.DailyQuotesDatabase,
        at: String,
        timeZone: TimeZone = TimeZone.UTC,
    ) = DefaultQuoteRepository(
        api = FavQsApi(buildTestClient(engine)),
        database = database,
        clock = FixedClock(Instant.parse(at)),
        timeZone = timeZone,
    )

    private fun qotdJson(id: Long, body: String, author: String = "Anon"): String = """
        {
          "qotd_date": "2026-08-25",
          "quote": { "id": $id, "body": "$body", "author": "$author", "favorites_count": 3 }
        }
    """.trimIndent()

    private class FixedClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }
}
