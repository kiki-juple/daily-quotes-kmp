package com.disheveled.dailyquotes.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module

actual val localModule: Module = module {
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = DailyQuotesDatabase.Schema,
            context = get<Context>(),
            name = DATABASE_NAME,
        )
    }
    single { DailyQuotesDatabase(get()) }
}
