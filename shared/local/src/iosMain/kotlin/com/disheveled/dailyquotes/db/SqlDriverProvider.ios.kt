package com.disheveled.dailyquotes.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual fun platformSqlDriver(): SqlDriver =
    NativeSqliteDriver(DailyQuotesDatabase.Schema, "dailyquotes.db")
