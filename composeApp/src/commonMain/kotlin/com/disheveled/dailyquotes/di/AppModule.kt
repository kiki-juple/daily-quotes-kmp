package com.disheveled.dailyquotes.di

import com.disheveled.dailyquotes.ui.favorites.FavoritesViewModel
import com.disheveled.dailyquotes.ui.home.HomeViewModel
import com.disheveled.dailyquotes.ui.login.LoginViewModel
import com.disheveled.dailyquotes.ui.register.RegisterViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

private val uiModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::FavoritesViewModel)
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(sharedDataModules() + uiModule)
    }
}
