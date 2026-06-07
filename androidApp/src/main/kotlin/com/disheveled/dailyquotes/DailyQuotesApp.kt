package com.disheveled.dailyquotes

import android.app.Application
import com.disheveled.dailyquotes.db.initAndroidPlatform
import com.disheveled.dailyquotes.di.initKoin
import org.koin.android.ext.koin.androidContext

class DailyQuotesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initAndroidPlatform(this)
        initKoin {
            androidContext(this@DailyQuotesApp)
        }
    }
}
