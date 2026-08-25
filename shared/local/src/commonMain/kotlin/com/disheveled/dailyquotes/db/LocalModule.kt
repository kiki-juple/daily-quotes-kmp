package com.disheveled.dailyquotes.db

import org.koin.core.module.Module

/**
 * Provides [DailyQuotesDatabase]. Declared per platform because the Android driver needs a
 * `Context`, which it takes from Koin — the same shape [com.disheveled.dailyquotes.data.api]
 * uses for its settings module.
 */
expect val localModule: Module
