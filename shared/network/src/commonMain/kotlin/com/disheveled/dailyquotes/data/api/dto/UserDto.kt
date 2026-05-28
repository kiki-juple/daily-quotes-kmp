package com.disheveled.dailyquotes.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterUserRequest(
    val user: RegisterUserBody,
)

@Serializable
data class RegisterUserBody(
    val login: String,
    val email: String,
    val password: String,
    @SerialName("password_confirmation") val passwordConfirmation: String,
)

@Serializable
data class UserDto(
    val login: String? = null,
    val email: String? = null,
    @SerialName("User-Token") val userToken: String? = null,
    @SerialName("Login") val loginUpper: String? = null,
    @SerialName("public_favorites_count") val publicFavoritesCount: Int? = null,
) {
    val effectiveLogin: String? get() = login ?: loginUpper
}
