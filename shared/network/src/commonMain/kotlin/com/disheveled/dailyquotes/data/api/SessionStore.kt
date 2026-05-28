package com.disheveled.dailyquotes.data.api

import com.russhwolf.settings.Settings

class SessionStore(private val settings: Settings) {

    var userToken: String?
        get() = settings.getStringOrNull(KEY_USER_TOKEN)
        set(value) {
            if (value == null) settings.remove(KEY_USER_TOKEN)
            else settings.putString(KEY_USER_TOKEN, value)
        }

    var login: String?
        get() = settings.getStringOrNull(KEY_LOGIN)
        set(value) {
            if (value == null) settings.remove(KEY_LOGIN)
            else settings.putString(KEY_LOGIN, value)
        }

    var email: String?
        get() = settings.getStringOrNull(KEY_EMAIL)
        set(value) {
            if (value == null) settings.remove(KEY_EMAIL)
            else settings.putString(KEY_EMAIL, value)
        }

    fun clear() {
        settings.remove(KEY_USER_TOKEN)
        settings.remove(KEY_LOGIN)
        settings.remove(KEY_EMAIL)
    }

    private companion object {
        const val KEY_USER_TOKEN = "user_token"
        const val KEY_LOGIN = "login"
        const val KEY_EMAIL = "email"
    }
}
