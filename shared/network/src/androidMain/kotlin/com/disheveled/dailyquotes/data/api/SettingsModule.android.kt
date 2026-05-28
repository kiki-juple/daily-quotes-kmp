package com.disheveled.dailyquotes.data.api

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module

actual val settingsModule: Module = module {
    single<Settings> {
        SharedPreferencesSettings(
            get<Context>().getSharedPreferences("dailyquotes", Context.MODE_PRIVATE)
        )
    }
}
