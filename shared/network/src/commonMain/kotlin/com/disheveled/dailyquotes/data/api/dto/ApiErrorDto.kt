package com.disheveled.dailyquotes.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorDto(
    val message: String? = null,
    @SerialName("error_code") val errorCode: Int? = null,
)
