package com.disheveled.dailyquotes.data.repository

import com.disheveled.dailyquotes.data.api.ApiException
import com.disheveled.dailyquotes.data.api.FavQsApi
import com.disheveled.dailyquotes.data.api.SessionStore
import com.russhwolf.settings.MapSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepositoryTest {

    @Test
    fun loginSuccessPopulatesUserAndPersistsSession() = runTest {
        val engine = MockEngine { request ->
            when {
                request.url.encodedPath.endsWith("/session") -> respondJson(
                    """{"User-Token":"tok-123","Login":"kiki","email":"k@example.com"}""",
                )

                request.url.encodedPath.endsWith("/users/kiki") -> respondJson(
                    """{"login":"kiki","email":"k@example.com"}""",
                )

                else -> error("Unexpected request: ${request.url}")
            }
        }
        val settings = MapSettings()
        val sessionStore = SessionStore(settings)
        val repo = AuthRepository(FavQsApi(buildClient(engine)), sessionStore)

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
    fun loginFailureClearsSession() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = """{"message":"Invalid credentials","error_code":401}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf("Content-Type", "application/json"),
            )
        }
        val settings = MapSettings().apply {
            putString("user_token", "stale-token")
            putString("login", "stale-login")
        }
        val sessionStore = SessionStore(settings)
        val repo = AuthRepository(FavQsApi(buildClient(engine)), sessionStore)

        val result = repo.login("kiki", "wrong")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApiException.Unauthorized)
        assertNull(sessionStore.userToken)
        assertNull(sessionStore.login)
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
        val repo = AuthRepository(FavQsApi(buildClient(engine)), sessionStore)

        // restored at construction
        assertNotNull(repo.currentUser.value)

        repo.logout()

        assertNull(repo.currentUser.value)
        assertNull(sessionStore.userToken)
        assertFalse(settings.hasKey("user_token"))
    }

    private fun buildClient(engine: MockEngine): HttpClient = HttpClient(engine) {
        expectSuccess = false
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true; explicitNulls = false })
        }
        defaultRequest {
            url {
                protocol = URLProtocol.HTTPS
                host = "favqs.com"
            }
        }
    }

    private fun io.ktor.client.engine.mock.MockRequestHandleScope.respondJson(body: String) =
        respond(
            content = body,
            status = HttpStatusCode.OK,
            headers = headersOf("Content-Type", "application/json"),
        )
}
