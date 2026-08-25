package com.disheveled.dailyquotes.data.repository

import com.disheveled.dailyquotes.data.api.ApiException
import com.disheveled.dailyquotes.data.api.FavQsApi
import com.disheveled.dailyquotes.data.api.SessionExpiredSignal
import com.disheveled.dailyquotes.data.api.SessionStore
import com.russhwolf.settings.MapSettings
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {

    @Test
    fun loginSuccessPopulatesUserAndPersistsSession() = runTest {
        val engine = MockEngine { request ->
            when {
                request.url.encodedPath.endsWith("/session") -> respondJson(
                    """{"User-Token":"tok-123","login":"kiki","email":"k@example.com"}""",
                )

                request.url.encodedPath.endsWith("/users/kiki") -> respondJson(
                    """{"login":"kiki","email":"k@example.com"}""",
                )

                else -> error("Unexpected request: ${request.url}")
            }
        }
        val settings = MapSettings()
        val sessionStore = SessionStore(settings)
        val cleaner = RecordingLocalDataCleaner()
        val repo = buildRepo(engine, sessionStore, cleaner)

        val result = repo.login("kiki", "secret")

        assertTrue(result.isSuccess, "expected success but got ${result.exceptionOrNull()}")
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals("kiki", user.login)
        assertEquals("k@example.com", user.email)
        assertEquals("tok-123", user.userToken)
        // SessionStore persisted via Settings
        assertEquals("tok-123", sessionStore.userToken)
        assertEquals("kiki", sessionStore.login)
        // currentUser flow emitted
        assertNotNull(repo.currentUser.value)
    }

    @Test
    fun loginWithEmailPersistsResolvedUsernameFromSession() = runTest {
        val engine = MockEngine { request ->
            when {
                request.url.encodedPath.endsWith("/session") -> respondJson(
                    """{"User-Token":"tok-123","login":"kiki","email":"k@example.com"}""",
                )

                request.url.encodedPath.endsWith("/users/kiki") -> respondJson(
                    """{"login":"kiki","email":"k@example.com"}""",
                )

                else -> error("Unexpected request: ${request.url}")
            }
        }
        val sessionStore = SessionStore(MapSettings())
        val cleaner = RecordingLocalDataCleaner()
        val repo = buildRepo(engine, sessionStore, cleaner)

        val result = repo.login("k@example.com", "secret")

        assertTrue(result.isSuccess, "expected success but got ${result.exceptionOrNull()}")
        assertEquals("kiki", result.getOrNull()?.login)
        assertEquals("kiki", sessionStore.login)
    }

    @Test
    fun loginFailureClearsSession() = runTest {
        val engine = MockEngine {
            respondJson(
                """{"message":"Invalid credentials","error_code":401}""",
                HttpStatusCode.Unauthorized,
            )
        }
        val settings = MapSettings().apply {
            putString("user_token", "stale-token")
            putString("login", "stale-login")
        }
        val sessionStore = SessionStore(settings)
        val cleaner = RecordingLocalDataCleaner()
        val repo = buildRepo(engine, sessionStore, cleaner)

        val result = repo.login("kiki", "wrong")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApiException.Unauthorized)
        assertNull(sessionStore.userToken)
        assertNull(sessionStore.login)
        assertNull(repo.currentUser.value)
    }

    @Test
    fun registerWithoutTokenFailsAndDoesNotPopulateCurrentUser() = runTest {
        val engine = MockEngine { _ ->
            respondJson("""{"login":"kiki","email":"k@example.com"}""")
        }
        val sessionStore = SessionStore(MapSettings())
        val cleaner = RecordingLocalDataCleaner()
        val repo = buildRepo(engine, sessionStore, cleaner)

        val result = repo.register("kiki", "k@example.com", "password123")

        assertTrue(result.isFailure)
        assertNull(sessionStore.userToken)
        assertNull(repo.currentUser.value)
    }

    @Test
    fun logoutClearsStateAndStore() = runTest {
        val settings = MapSettings().apply {
            putString("user_token", "tok")
            putString("login", "kiki")
        }
        val sessionStore = SessionStore(settings)
        val engine = MockEngine { error("API should not be called during logout") }
        val cleaner = RecordingLocalDataCleaner()
        val repo = buildRepo(engine, sessionStore, cleaner)

        // restored at construction
        assertNotNull(repo.currentUser.value)

        repo.logout()

        assertNull(repo.currentUser.value)
        assertNull(sessionStore.userToken)
        assertFalse(settings.hasKey("user_token"))
        assertEquals(1, cleaner.clearCalls, "logout must drop the cached favorites")
    }

    @Test
    fun loginAsDifferentAccountClearsPreviousAccountLocalData() = runTest {
        val engine = MockEngine { request ->
            when {
                request.url.encodedPath.endsWith("/session") ->
                    respondJson("""{"User-Token":"tok-b","login":"budi","email":"budi@example.com"}""")

                else -> respondJson("""{"login":"budi","email":"budi@example.com"}""")
            }
        }
        // A session left behind by another account.
        val sessionStore = SessionStore(
            MapSettings().apply {
                putString("user_token", "tok-a")
                putString("login", "kiki")
            },
        )
        val cleaner = RecordingLocalDataCleaner()
        val repo = buildRepo(engine, sessionStore, cleaner)

        val result = repo.login("budi", "secret123")

        assertTrue(result.isSuccess, "expected success but got ${result.exceptionOrNull()}")
        assertEquals(1, cleaner.clearCalls, "favorites of the previous account must be dropped")
        assertEquals("budi", sessionStore.login)
    }

    @Test
    fun loginAsSameAccountKeepsLocalData() = runTest {
        val engine = MockEngine { request ->
            when {
                request.url.encodedPath.endsWith("/session") ->
                    respondJson("""{"User-Token":"tok","login":"kiki","email":"kiki@example.com"}""")

                else -> respondJson("""{"login":"kiki","email":"kiki@example.com"}""")
            }
        }
        val sessionStore = SessionStore(MapSettings().apply { putString("login", "kiki") })
        val cleaner = RecordingLocalDataCleaner()
        val repo = buildRepo(engine, sessionStore, cleaner)

        val result = repo.login("kiki", "secret123")

        assertTrue(result.isSuccess, "expected success but got ${result.exceptionOrNull()}")
        assertEquals(0, cleaner.clearCalls, "the same account keeps its cached favorites")
    }

    @Test
    fun sessionExpirySignalLogsOutAndClearsLocalData() = runTest {
        val settings = MapSettings().apply {
            putString("user_token", "stale")
            putString("login", "kiki")
        }
        val sessionStore = SessionStore(settings)
        val cleaner = RecordingLocalDataCleaner()
        val signal = SessionExpiredSignal()
        val engine = MockEngine { error("API should not be called when the session expires") }
        val repo = buildRepo(engine, sessionStore, cleaner, signal)
        // The expiry collector is launched from init; let it subscribe before emitting, otherwise
        // the (replay-free) event has nobody to reach.
        runCurrent()

        assertNotNull(repo.currentUser.value, "restored from the stored session")

        signal.notifyExpired()
        runCurrent()

        assertNull(repo.currentUser.value, "a rejected token must drop the user to the auth wall")
        assertNull(sessionStore.userToken)
        assertEquals(1, cleaner.clearCalls, "the dead session's favorites must not linger")
    }

    @Test
    fun sessionExpirySignalIsIgnoredWhenAlreadyLoggedOut() = runTest {
        val sessionStore = SessionStore(MapSettings())
        val cleaner = RecordingLocalDataCleaner()
        val signal = SessionExpiredSignal()
        val engine = MockEngine { error("API should not be called when the session expires") }
        val repo = buildRepo(engine, sessionStore, cleaner, signal)
        runCurrent()

        assertNull(repo.currentUser.value)

        signal.notifyExpired()
        runCurrent()

        assertEquals(0, cleaner.clearCalls, "no session means nothing to tear down")
    }

    private fun TestScope.buildRepo(
        engine: MockEngine,
        sessionStore: SessionStore,
        cleaner: LocalDataCleaner,
        signal: SessionExpiredSignal = SessionExpiredSignal(),
    ) = DefaultAuthRepository(
        api = FavQsApi(buildTestClient(engine)),
        sessionStore = sessionStore,
        localDataCleaner = cleaner,
        sessionExpiredSignal = signal,
        scope = backgroundScope,
    )

    private class RecordingLocalDataCleaner : LocalDataCleaner {
        var clearCalls = 0
            private set

        override suspend fun clearAll() {
            clearCalls += 1
        }
    }

}
