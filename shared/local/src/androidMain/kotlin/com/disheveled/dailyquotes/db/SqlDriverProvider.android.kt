package com.disheveled.dailyquotes.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

@Volatile
private var appContext: Context? = null

fun initAndroidPlatform(context: Context) {
    appContext = context.applicationContext
}

actual fun platformSqlDriver(): SqlDriver {
    val ctx = appContext ?: error("Call initAndroidPlatform(context) before resolving Koin")
    return AndroidSqliteDriver(
        schema = DailyQuotesDatabase.Schema,
        context = ctx,
        name = "dailyquotes.db",
    )
}
