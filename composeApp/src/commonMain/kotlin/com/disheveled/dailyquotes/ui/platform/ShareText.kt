package com.disheveled.dailyquotes.ui.platform

import androidx.compose.runtime.Composable
import com.disheveled.dailyquotes.domain.model.Quote

@Composable
expect fun rememberShareText(): (String) -> Boolean

fun quoteShareText(quote: Quote): String =
    "\"${quote.body.trim()}\"\n- ${quote.author}\n\nRenung"
