package com.disheveled.dailyquotes.data.api

import org.koin.dsl.module

val networkModule = module {
    single { platformHttpClientEngine() }
    single { SessionStore(get()) }
    single { SessionExpiredSignal() }
    single { createHttpClient(engine = get(), session = get(), sessionExpired = get()) }
    single { FavQsApi(get()) }
}
