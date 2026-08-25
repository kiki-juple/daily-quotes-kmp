package com.disheveled.dailyquotes.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module

actual val localModule: Module = module {
    single<SqlDriver> { NativeSqliteDriver(DailyQuotesDatabase.Schema, DATABASE_NAME) }
    single { DailyQuotesDatabase(get()) }
}
