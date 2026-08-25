package com.disheveled.dailyquotes.data.repository

import com.disheveled.dailyquotes.data.api.ApiException
import com.disheveled.dailyquotes.data.api.FavQsApi
import com.disheveled.dailyquotes.data.api.SessionExpiredSignal
import com.disheveled.dailyquotes.data.api.SessionStore
import com.disheveled.dailyquotes.data.util.resultOf
import com.disheveled.dailyquotes.domain.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface AuthRepository {
    val currentUser: StateFlow<User?>
    suspend fun login(login: String, password: String): Result<User>
    suspend fun register(login: String, email: String, password: String): Result<User>
    suspend fun logout()
}

class DefaultAuthRepository(
    private val api: FavQsApi,
    private val sessionStore: SessionStore,
    private val localDataCleaner: LocalDataCleaner,
    sessionExpiredSignal: SessionExpiredSignal,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(restoredUser())
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        // The server rejected our token, so the stored session is worthless. Log out instead of
        // leaving the user parked on a screen that can only ever show errors.
        scope.launch {
            sessionExpiredSignal.events.collect {
                if (_currentUser.value != null) logout()
            }
        }
    }

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
        clearLocalDataIfAccountChanged(resolvedLogin)
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
            clearLocalDataIfAccountChanged(resolved)
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

    override suspend fun logout() {
        // Drop cached rows before flipping [currentUser], so navigation only swaps to the auth wall
        // once the previous account's favorites are already gone.
        localDataCleaner.clearAll()
        sessionStore.clear()
        _currentUser.value = null
    }

    /**
     * A stored login that differs from the one we just authenticated as means the cached favorites
     * belong to somebody else — for example when the process was killed before [logout] ran.
     */
    private suspend fun clearLocalDataIfAccountChanged(resolvedLogin: String) {
        val previousLogin = sessionStore.login
        if (previousLogin == null || !previousLogin.equals(resolvedLogin, ignoreCase = true)) {
            localDataCleaner.clearAll()
        }
    }
}
