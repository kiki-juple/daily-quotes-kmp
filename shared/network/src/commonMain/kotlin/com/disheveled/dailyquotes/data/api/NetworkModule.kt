package com.disheveled.dailyquotes.data.api

import org.koin.dsl.module

val networkModule = module {
    single { platformHttpClientEngine() }
    single { SessionStore(get()) }
    single { createHttpClient(get(), get()) }
    single { FavQsApi(get()) }
}
