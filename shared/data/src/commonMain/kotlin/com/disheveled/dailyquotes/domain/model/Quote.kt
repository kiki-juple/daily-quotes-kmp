package com.disheveled.dailyquotes.domain.model

data class Quote(
    val id: Long,
    val body: String,
    val author: String,
    val favoritesCount: Int,
)
