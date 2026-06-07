package com.disheveled.dailyquotes.data.api

import com.russhwolf.settings.MapSettings
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
        val api = FavQsApi(createHttpClient(engine, sessionStore))

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
        val api = FavQsApi(createHttpClient(engine, sessionStore))

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
        val api = FavQsApi(createHttpClient(engine, sessionStore))

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
        val api = FavQsApi(createHttpClient(engine, SessionStore(MapSettings())))

        val error = assertFailsWith<ApiException.ApiError> {
            api.favoriteQuote(404)
        }

        assertEquals(40, error.errorCode)
        assertEquals("Quote not found.", error.message)
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
