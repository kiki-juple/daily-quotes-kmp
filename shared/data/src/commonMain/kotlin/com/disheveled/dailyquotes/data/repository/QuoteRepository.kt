package com.disheveled.dailyquotes.data.repository

import com.disheveled.dailyquotes.data.api.FavQsApi
import com.disheveled.dailyquotes.data.util.resultOf
import com.disheveled.dailyquotes.db.DailyQuotesDatabase
import com.disheveled.dailyquotes.domain.model.Quote
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class QuoteRepository(
    private val api: FavQsApi,
    private val database: DailyQuotesDatabase,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {

    suspend fun getQuoteOfTheDay(): Result<Quote> = resultOf {
        val today = clock.now().toLocalDateTime(timeZone).date.toString()
        val cached = database.quoteOfTheDayQueries.selectForDate(today).executeAsOneOrNull()
        if (cached != null) {
            return@resultOf Quote(
                id = cached.id,
                body = cached.body,
                author = cached.author,
                favoritesCount = cached.favoritesCount.toInt(),
            )
        }
        val dto = api.getQuoteOfTheDay().quote
        database.quoteOfTheDayQueries.upsert(
            qotdDate = today,
            id = dto.id,
            body = dto.body,
            author = dto.author,
            favoritesCount = dto.favoritesCount.toLong(),
        )
        database.quoteOfTheDayQueries.deleteOlderThan(today)
        Quote(
            id = dto.id,
            body = dto.body,
            author = dto.author,
            favoritesCount = dto.favoritesCount,
        )
    }
}
