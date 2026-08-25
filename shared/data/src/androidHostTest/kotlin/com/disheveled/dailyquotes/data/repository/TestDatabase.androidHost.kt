package com.disheveled.dailyquotes.data.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.disheveled.dailyquotes.db.DailyQuotesDatabase

actual fun inMemorySqlDriver(): SqlDriver =
    JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { DailyQuotesDatabase.Schema.create(it) }
