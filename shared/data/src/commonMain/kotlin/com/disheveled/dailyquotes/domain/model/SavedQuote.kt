package com.disheveled.dailyquotes.domain.model

/**
 * A favorited [Quote] together with when it was saved.
 *
 * `savedAtEpochMs` already drives the ordering of the favorites list; carrying it into the domain
 * lets the UI show the real date instead of a hardcoded label.
 */
data class SavedQuote(
    val quote: Quote,
    val savedAtEpochMs: Long,
)
