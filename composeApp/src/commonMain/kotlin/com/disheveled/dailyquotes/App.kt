package com.disheveled.dailyquotes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.disheveled.dailyquotes.ui.nav.AppNavHost
import com.disheveled.dailyquotes.ui.theme.DailyQuotesTheme
import com.disheveled.dailyquotes.ui.theme.RenungColors

@Composable
fun App() {
    DailyQuotesTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RenungColors.Paper),
        ) {
            AppNavHost()
        }
    }
}
