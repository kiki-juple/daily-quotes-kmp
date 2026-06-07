package com.disheveled.dailyquotes.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateSessionRequest(
    val user: CreateSessionBody,
)

@Serializable
data class CreateSessionBody(
    val login: String,
    val password: String,
)

@Serializable
data class SessionDto(
    @SerialName("User-Token") val userToken: String? = null,
    val login: String? = null,
    @SerialName("Login") val loginUpper: String? = null,
    val email: String? = null,
) {
    val effectiveLogin: String? get() = login ?: loginUpper
}
