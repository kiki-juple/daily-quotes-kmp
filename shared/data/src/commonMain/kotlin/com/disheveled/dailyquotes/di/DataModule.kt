package com.disheveled.dailyquotes.di

import com.disheveled.dailyquotes.data.api.networkModule
import com.disheveled.dailyquotes.data.api.settingsModule
import com.disheveled.dailyquotes.data.repository.AuthRepository
import com.disheveled.dailyquotes.data.repository.DefaultAuthRepository
import com.disheveled.dailyquotes.data.repository.DefaultFavoritesRepository
import com.disheveled.dailyquotes.data.repository.DefaultLocalDataCleaner
import com.disheveled.dailyquotes.data.repository.DefaultQuoteRepository
import com.disheveled.dailyquotes.data.repository.FavoritesRepository
import com.disheveled.dailyquotes.data.repository.LocalDataCleaner
import com.disheveled.dailyquotes.data.repository.QuoteRepository
import com.disheveled.dailyquotes.db.localModule
import org.koin.core.module.Module
import org.koin.dsl.module

val dataModule = module {
    single<LocalDataCleaner> { DefaultLocalDataCleaner(database = get()) }
    single<AuthRepository> {
        DefaultAuthRepository(
            api = get(),
            sessionStore = get(),
            localDataCleaner = get(),
            sessionExpiredSignal = get(),
        )
    }
    single<QuoteRepository> { DefaultQuoteRepository(api = get(), database = get()) }
    single<FavoritesRepository> {
        DefaultFavoritesRepository(api = get(), database = get(), sessionStore = get())
    }
}

fun sharedDataModules(): List<Module> =
    listOf(settingsModule, networkModule, localModule, dataModule)
