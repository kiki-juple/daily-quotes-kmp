package com.disheveled.dailyquotes.data.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.disheveled.dailyquotes.db.DailyQuotesDatabase

private var databaseCount = 0

actual fun inMemorySqlDriver(): SqlDriver = NativeSqliteDriver(
    schema = DailyQuotesDatabase.Schema,
    // sqliter shares in-memory databases that carry the same name, so reusing one name would let
    // rows leak from one test into the next. Each driver gets its own.
    name = "dailyquotes-test-${databaseCount++}.db",
    onConfiguration = { it.copy(inMemory = true) },
)
