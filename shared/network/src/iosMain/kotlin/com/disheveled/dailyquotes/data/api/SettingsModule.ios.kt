package com.disheveled.dailyquotes.data.api

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module

@OptIn(ExperimentalSettingsImplementation::class)
actual val settingsModule: Module = module {
    single<Settings> {
        KeychainSettings("com.disheveled.dailyquotes.session")
    }
}
