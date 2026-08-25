package com.disheveled.dailyquotes.data.repository

import app.cash.sqldelight.db.SqlDriver
import com.disheveled.dailyquotes.db.DailyQuotesDatabase

/**
 * A fresh, empty in-memory database. Nothing touches the filesystem and no state leaks between
 * tests, so repository behaviour can be asserted against real SQL instead of a hand-written fake
 * that would happily disagree with the schema.
 */
expect fun inMemorySqlDriver(): SqlDriver

fun inMemoryDatabase(): DailyQuotesDatabase = DailyQuotesDatabase(inMemorySqlDriver())
