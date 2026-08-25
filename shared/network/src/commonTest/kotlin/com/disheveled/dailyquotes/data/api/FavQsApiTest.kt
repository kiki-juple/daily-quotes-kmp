package com.disheveled.dailyquotes.data.api

import com.russhwolf.settings.MapSettings
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.pluginOrNull
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FavQsApiTest {

    @Test
    fun getFavoriteQuotesUsesUserFilterEndpoint() = runBlocking {
        val sessionStore = SessionStore(
            MapSettings().apply {
                putString("user_token", "tok-123")
            }
        )
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("/api/quotes", request.url.encodedPath)
            assertEquals("kiki", request.url.parameters["filter"])
            assertEquals("user", request.url.parameters["type"])
            assertEquals("2", request.url.parameters["page"])
            assertEquals("tok-123", request.headers["User-Token"])
            respondJson(
                """
                {
                  "page": 2,
                  "last_page": true,
                  "quotes": [
                    {
                      "id": 42,
                      "author": "Renung",
                      "body": "Tetap jalan pelan-pelan.",
                      "favorites_count": 8
                    }
                  ]
                }
                """.trimIndent(),
            )
        }
        val api = FavQsApi(createHttpClient(engine, sessionStore, SessionExpiredSignal()))

        val response = api.getFavoriteQuotes(login = "kiki", page = 2)

        assertEquals(2, response.page)
        assertEquals(true, response.lastPage)
        assertEquals(42, response.quotes.single().id)
    }

    @Test
    fun favoriteQuoteUsesFavEndpointWithSessionToken() = runBlocking {
        val sessionStore = SessionStore(
            MapSettings().apply {
                putString("user_token", "tok-123")
            }
        )
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Put, request.method)
            assertEquals("/api/quotes/42/fav", request.url.encodedPath)
            assertEquals("tok-123", request.headers["User-Token"])
            respondJson(
                """
                {
                  "id": 42,
                  "author": "Renung",
                  "body": "Tetap jalan pelan-pelan.",
                  "favorites_count": 8,
                  "user_details": {
                    "favorite": true
                  }
                }
                """.trimIndent(),
            )
        }
        val api = FavQsApi(createHttpClient(engine, sessionStore, SessionExpiredSignal()))

        val quote = api.favoriteQuote(42)

        assertEquals(42, quote.id)
        assertEquals(8, quote.favoritesCount)
    }

    @Test
    fun unfavoriteQuoteUsesUnfavEndpoint() = runBlocking {
        val sessionStore = SessionStore(
            MapSettings().apply {
                putString("user_token", "tok-123")
            }
        )
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Put, request.method)
            assertEquals("/api/quotes/42/unfav", request.url.encodedPath)
            respondJson(
                """
                {
                  "id": 42,
                  "author": "Renung",
                  "body": "Tetap jalan pelan-pelan.",
                  "favorites_count": 7,
                  "user_details": {
                    "favorite": false
                  }
                }
                """.trimIndent(),
            )
        }
        val api = FavQsApi(createHttpClient(engine, sessionStore, SessionExpiredSignal()))

        val quote = api.unfavoriteQuote(42)

        assertEquals(42, quote.id)
        assertEquals(7, quote.favoritesCount)
    }

    @Test
    fun favoriteQuoteSurfacesFavQsErrorCode() = runBlocking {
        val engine = MockEngine {
            respondJson(
                """{"error_code":40,"message":"Quote not found."}""",
            )
        }
        val api = FavQsApi(createHttpClient(engine, SessionStore(MapSettings()), SessionExpiredSignal()))

        val error = assertFailsWith<ApiException.ApiError> {
            api.favoriteQuote(404)
        }

        assertEquals(40, error.errorCode)
        assertEquals("Quote not found.", error.message)
    }

    @Test
    fun unauthorizedOnAuthenticatedRequestSignalsSessionExpiry() = runBlocking {
        val signal = SessionExpiredSignal()
        val expired = CompletableDeferred<Unit>()
        val subscribed = CompletableDeferred<Unit>()
        val collector = launch {
            signal.events
                .onSubscription { subscribed.complete(Unit) }
                .collect { expired.complete(Unit) }
        }
        subscribed.await()

        val engine = MockEngine {
            respondJson("""{"message":"Unauthorized."}""", HttpStatusCode.Unauthorized)
        }
        val sessionStore = SessionStore(MapSettings().apply { putString("user_token", "stale") })
        val api = FavQsApi(createHttpClient(engine, sessionStore, signal))

        assertFailsWith<ApiException.Unauthorized> { api.favoriteQuote(42) }

        withTimeout(5_000) { expired.await() }
        collector.cancel()
    }

    @Test
    fun unauthorizedWithoutSessionTokenDoesNotSignalSessionExpiry() = runBlocking {
        val signal = SessionExpiredSignal()
        val expired = CompletableDeferred<Unit>()
        val subscribed = CompletableDeferred<Unit>()
        val collector = launch {
            signal.events
                .onSubscription { subscribed.complete(Unit) }
                .collect { expired.complete(Unit) }
        }
        subscribed.await()

        // No stored token: this is the shape of a wrong-password login, not an expired session.
        val engine = MockEngine {
            respondJson("""{"message":"Unauthorized."}""", HttpStatusCode.Unauthorized)
        }
        val api = FavQsApi(createHttpClient(engine, SessionStore(MapSettings()), signal))

        assertFailsWith<ApiException.Unauthorized> { api.createSession("kiki", "wrong") }

        val fired = withTimeoutOrNull(500) { expired.await() }
        collector.cancel()
        assertNull(fired, "a wrong password must not trip the auto-logout")
    }

    @Test
    fun successfulPayloadCarryingAMessageFieldIsStillParsed() = runBlocking {
        // The old key-sniffing heuristic flagged any body with `message` and none of a hand-written
        // whitelist of keys as an error, so a perfectly good quote list was rejected.
        val engine = MockEngine {
            respondJson(
                """
                {
                  "page": 1,
                  "last_page": true,
                  "message": "Showing your favorites.",
                  "quotes": [{"id": 5, "body": "Ada.", "author": "Anon", "favorites_count": 1}]
                }
                """.trimIndent(),
            )
        }
        val api = FavQsApi(createHttpClient(engine, SessionStore(MapSettings()), SessionExpiredSignal()))

        val response = api.getFavoriteQuotes(login = "kiki")

        assertEquals(5L, response.quotes.single().id)
    }

    @Test
    fun clientConfiguresExplicitTimeouts() {
        // Guards against dropping the plugin: without it the timeout is engine-dependent and
        // FavQsApi's HttpRequestTimeoutException branch is unreachable.
        val client = createHttpClient(MockEngine { respondJson("{}") }, SessionStore(MapSettings()), SessionExpiredSignal())
        val timeout = client.pluginOrNull(HttpTimeout)

        assertNotNull(timeout, "HttpTimeout must be installed")
    }

    private fun MockRequestHandleScope.respondJson(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ) = respond(
        content = body,
        status = status,
        headers = headersOf("Content-Type", "application/json"),
    )
}
