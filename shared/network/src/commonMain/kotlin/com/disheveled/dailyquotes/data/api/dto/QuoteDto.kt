package com.disheveled.dailyquotes.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuoteDto(
    val id: Long,
    val body: String,
    val author: String = "Unknown",
    @SerialName("favorites_count") val favoritesCount: Int = 0,
    val tags: List<String> = emptyList(),
)

@Serializable
data class QuoteOfTheDayDto(
    @SerialName("qotd_date") val qotdDate: String? = null,
    val quote: QuoteDto,
)
