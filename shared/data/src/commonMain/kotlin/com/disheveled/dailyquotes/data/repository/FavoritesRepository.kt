package com.disheveled.dailyquotes.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.disheveled.dailyquotes.db.DailyQuotesDatabase
import com.disheveled.dailyquotes.db.FavoriteQuote
import com.disheveled.dailyquotes.domain.model.Quote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class FavoritesRepository(private val database: DailyQuotesDatabase) {

    fun observeFavorites(): Flow<List<Quote>> =
        database.favoriteQuoteQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toQuote() } }

    fun observeIsFavorite(quoteId: Long): Flow<Boolean> =
        database.favoriteQuoteQueries.countById(quoteId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { (it ?: 0L) > 0L }

    suspend fun add(quote: Quote) {
        database.favoriteQuoteQueries.upsert(
            id = quote.id,
            body = quote.body,
            author = quote.author,
            favoritesCount = quote.favoritesCount.toLong(),
            savedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
        )
    }

    suspend fun remove(quoteId: Long) {
        database.favoriteQuoteQueries.deleteById(quoteId)
    }

    private fun FavoriteQuote.toQuote(): Quote = Quote(
        id = id,
        body = body,
        author = author,
        favoritesCount = favoritesCount.toInt(),
    )
}
