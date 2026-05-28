package com.disheveled.dailyquotes.db

import org.koin.dsl.module

val localModule = module {
    single { platformSqlDriver() }
    single { DailyQuotesDatabase(get()) }
}
