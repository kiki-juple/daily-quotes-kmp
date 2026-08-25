package com.disheveled.dailyquotes.data.repository

import com.disheveled.dailyquotes.db.DailyQuotesDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Wipes every locally cached row tied to the signed-in account.
 *
 * Favorites and the cached quote-of-the-day are per-user data with no owner column, so they have to
 * be dropped whenever the account changes — on logout, and on a login that resolves to a different
 * username than the one already stored. Without this the next account sees the previous account's
 * favorites until (and unless) a remote refresh succeeds.
 */
interface LocalDataCleaner {
    suspend fun clearAll()
}

class DefaultLocalDataCleaner(
    private val database: DailyQuotesDatabase,
) : LocalDataCleaner {

    override suspend fun clearAll(): Unit = withContext(Dispatchers.Default) {
        database.transaction {
            database.favoriteQuoteQueries.deleteAll()
            database.quoteOfTheDayQueries.deleteAll()
        }
    }
}
