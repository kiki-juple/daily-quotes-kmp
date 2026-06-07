package com.disheveled.dailyquotes.data.repository

import com.disheveled.dailyquotes.data.api.ApiException
import com.disheveled.dailyquotes.data.api.FavQsApi
import com.disheveled.dailyquotes.data.api.SessionStore
import com.disheveled.dailyquotes.data.util.resultOf
import com.disheveled.dailyquotes.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>
    suspend fun login(login: String, password: String): Result<User>
    suspend fun register(login: String, email: String, password: String): Result<User>
    fun logout()
}

class DefaultAuthRepository(
    private val api: FavQsApi,
    private val sessionStore: SessionStore,
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(restoredUser())
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private fun restoredUser(): User? {
        val token = sessionStore.userToken ?: return null
        val login = sessionStore.login ?: return null
        return User(login = login, email = sessionStore.email, userToken = token)
    }

    override suspend fun login(login: String, password: String): Result<User> = resultOf {
        val session = api.createSession(login.trim(), password)
        val token = session.userToken
            ?: throw ApiException.ApiError(
                session.effectiveLogin?.let { "Tidak dapat masuk" } ?: "Username atau sandi salah",
            )
        val resolvedLogin = session.effectiveLogin ?: login.trim()
        sessionStore.userToken = token
        sessionStore.login = resolvedLogin
        sessionStore.email = session.email

        val profile = runCatching { api.getUser(resolvedLogin) }.getOrNull()
        val user = User(
            login = profile?.effectiveLogin ?: resolvedLogin,
            email = profile?.email ?: session.email,
            userToken = token,
        )
        sessionStore.email = user.email
        _currentUser.value = user
        user
    }.onFailure {
        sessionStore.clear()
        _currentUser.value = null
    }

    override suspend fun register(login: String, email: String, password: String): Result<User> =
        resultOf {
            val dto = api.register(login.trim(), email.trim(), password)
            val token = dto.userToken
                ?: throw ApiException.ApiError("Tidak dapat membuat akun")
            val resolved = dto.effectiveLogin ?: login.trim()
            sessionStore.userToken = token
            sessionStore.login = resolved
            sessionStore.email = email
            val user = User(
                login = resolved,
                email = email,
                userToken = token,
            )
            _currentUser.value = user
            user
        }

    override fun logout() {
        sessionStore.clear()
        _currentUser.value = null
    }
}
