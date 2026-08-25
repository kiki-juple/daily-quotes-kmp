package com.disheveled.dailyquotes

import android.app.Application
import com.disheveled.dailyquotes.di.initKoin
import org.koin.android.ext.koin.androidContext

class DailyQuotesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@DailyQuotesApp)
        }
    }
}
