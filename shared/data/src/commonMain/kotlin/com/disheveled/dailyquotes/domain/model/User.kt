package com.disheveled.dailyquotes.domain.model

data class User(
    val login: String,
    val email: String? = null,
    val userToken: String? = null,
)
