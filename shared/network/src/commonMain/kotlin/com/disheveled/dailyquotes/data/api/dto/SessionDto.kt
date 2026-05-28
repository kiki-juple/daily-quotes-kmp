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
    @SerialName("Login") val login: String? = null,
    val email: String? = null,
)
