package com.disheveled.dailyquotes

import androidx.compose.ui.window.ComposeUIViewController
import com.disheveled.dailyquotes.di.initKoin

private var koinStarted = false

fun startKoinIfNeeded() {
    if (!koinStarted) {
        initKoin()
        koinStarted = true
    }
}

fun MainViewController() = ComposeUIViewController {
    startKoinIfNeeded()
    App()
}
