package com.disheveled.dailyquotes.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.disheveled.dailyquotes.data.api.FavQsApi
import com.disheveled.dailyquotes.data.api.SessionStore
import com.disheveled.dailyquotes.data.api.dto.QuoteDto
import com.disheveled.dailyquotes.db.DailyQuotesDatabase
import com.disheveled.dailyquotes.db.FavoriteQuote
import com.disheveled.dailyquotes.domain.model.Quote
import com.disheveled.dailyquotes.domain.model.SavedQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
interface FavoritesRepository {
    fun observeFavorites(): Flow<List<SavedQuote>>
    fun observeIsFavorite(quoteId: Long): Flow<Boolean>
    suspend fun refreshFavorites()
    suspend fun add(quote: Quote)
    suspend fun remove(quoteId: Long)
}

@OptIn(ExperimentalTime::class)
class DefaultFavoritesRepository(
    private val api: FavQsApi,
    private val database: DailyQuotesDatabase,
    private val sessionStore: SessionStore,
) : FavoritesRepository {

    override fun observeFavorites(): Flow<List<SavedQuote>> =
        database.favoriteQuoteQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toSavedQuote() } }

    override fun observeIsFavorite(quoteId: Long): Flow<Boolean> =
        database.favoriteQuoteQueries.countById(quoteId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { (it ?: 0L) > 0L }

    override suspend fun refreshFavorites() {
        val login = sessionStore.login?.trim()?.takeIf { it.isNotEmpty() } ?: return
        val favorites = mutableListOf<QuoteDto>()
        var page = 1
        do {
            val response = api.getFavoriteQuotes(login = login, page = page)
            favorites += response.quotes
            // A server that never sets last_page would otherwise spin here forever, accumulating
            // every page in memory. Stop at the cap and keep what we have.
            val hasNextPage = !response.lastPage &&
                    response.quotes.isNotEmpty() &&
                    page < MAX_FAVORITE_PAGES
            page += 1
        } while (hasNextPage)

        val savedAtEpochMs = Clock.System.now().toEpochMilliseconds()
        database.transaction {
            database.favoriteQuoteQueries.deleteAll()
            favorites.forEachIndexed { index, quote ->
                database.favoriteQuoteQueries.upsert(
                    id = quote.id,
                    body = quote.body,
                    author = quote.author,
                    favoritesCount = quote.favoritesCount.toLong(),
                    savedAtEpochMs = savedAtEpochMs - index,
                )
            }
        }
    }

    override suspend fun add(quote: Quote) {
        val favorites = api.favoriteQuote(quote.id)
        database.favoriteQuoteQueries.upsert(
            id = favorites.id,
            body = favorites.body,
            author = favorites.author,
            favoritesCount = favorites.favoritesCount.toLong(),
            savedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
        )
    }

    override suspend fun remove(quoteId: Long) {
        api.unfavoriteQuote(quoteId)
        database.favoriteQuoteQueries.deleteById(quoteId)
    }

    private companion object {
        const val MAX_FAVORITE_PAGES = 20
    }

    private fun FavoriteQuote.toSavedQuote(): SavedQuote = SavedQuote(
        quote = Quote(
            id = id,
            body = body,
            author = author,
            favoritesCount = favoritesCount.toInt(),
        ),
        savedAtEpochMs = savedAtEpochMs,
    )
}
