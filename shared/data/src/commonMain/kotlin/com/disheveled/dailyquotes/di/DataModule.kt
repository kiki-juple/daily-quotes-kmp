package com.disheveled.dailyquotes.di

import com.disheveled.dailyquotes.data.api.networkModule
import com.disheveled.dailyquotes.data.api.settingsModule
import com.disheveled.dailyquotes.data.repository.AuthRepository
import com.disheveled.dailyquotes.data.repository.FavoritesRepository
import com.disheveled.dailyquotes.data.repository.QuoteRepository
import com.disheveled.dailyquotes.db.localModule
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    singleOf(::AuthRepository)
    single { QuoteRepository(api = get(), database = get()) }
    singleOf(::FavoritesRepository)
}

fun sharedDataModules(): List<Module> =
    listOf(settingsModule, networkModule, localModule, dataModule)
