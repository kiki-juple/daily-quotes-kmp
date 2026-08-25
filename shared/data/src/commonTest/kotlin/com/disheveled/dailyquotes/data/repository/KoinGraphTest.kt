package com.disheveled.dailyquotes.data.repository

import app.cash.sqldelight.db.SqlDriver
import com.disheveled.dailyquotes.data.api.FavQsApi
import com.disheveled.dailyquotes.data.api.SessionExpiredSignal
import com.disheveled.dailyquotes.data.api.SessionStore
import com.disheveled.dailyquotes.data.api.networkModule
import com.disheveled.dailyquotes.db.DailyQuotesDatabase
import com.disheveled.dailyquotes.di.dataModule
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Resolves the real Koin graph.
 *
 * Every other test constructs repositories by hand, so a definition registered under the wrong type
 * — `single { AndroidSqliteDriver(...) }` instead of `single<SqlDriver> { ... }`, say — compiles,
 * passes the whole suite, and only blows up when the app starts.
 *
 * Caveat: the platform modules (`localModule`, `settingsModule`) need a `Context` on Android and are
 * stubbed here, so their actuals are still only covered by launching the app.
 */
class KoinGraphTest {

    @Test
    fun everyRepositoryResolvesFromTheRealModules() {
        val platformStubs = module {
            single<Settings> { MapSettings() }
            single<SqlDriver> { inMemorySqlDriver() }
            single { DailyQuotesDatabase(get()) }
        }
        val app = koinApplication { modules(platformStubs, networkModule, dataModule) }

        try {
            val koin = app.koin
            assertNotNull(koin.get<SessionStore>())
            assertNotNull(koin.get<SessionExpiredSignal>())
            assertNotNull(koin.get<HttpClient>())
            assertNotNull(koin.get<FavQsApi>())
            assertNotNull(koin.get<LocalDataCleaner>())
            assertNotNull(koin.get<AuthRepository>())
            assertNotNull(koin.get<QuoteRepository>())
            assertNotNull(koin.get<FavoritesRepository>())
        } finally {
            app.close()
        }
    }
}
