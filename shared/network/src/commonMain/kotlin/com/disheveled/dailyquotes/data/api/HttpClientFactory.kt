package com.disheveled.dailyquotes.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal const val HEADER_USER_TOKEN: String = "User-Token"

object FavQsConfig {
    const val BASE_HOST: String = "favqs.com"
    const val BASE_PATH: String = "/api"

    // Without these the timeout is whatever the platform engine defaults to — roughly 10s on OkHttp
    // and 60s on Darwin — so Android and iOS would give up at wildly different points, and
    // FavQsApi's HttpRequestTimeoutException handling would never run at all.
    const val CONNECT_TIMEOUT_MS: Long = 15_000
    const val REQUEST_TIMEOUT_MS: Long = 30_000
    const val SOCKET_TIMEOUT_MS: Long = 30_000

    // Sourced from local.properties (favqs.api.key) or env FAVQS_API_KEY via :shared:network generateApiKey task.
    val API_KEY: String get() = FAVQS_API_KEY

    /** One wire-format configuration, shared by ContentNegotiation and FavQsApi's manual parsing. */
    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }
}

fun createHttpClient(
    engine: HttpClientEngine,
    session: SessionStore,
    sessionExpired: SessionExpiredSignal,
): HttpClient =
    HttpClient(engine) {
        expectSuccess = false

        install(ContentNegotiation) {
            json(FavQsConfig.json)
        }

        install(HttpTimeout) {
            connectTimeoutMillis = FavQsConfig.CONNECT_TIMEOUT_MS
            requestTimeoutMillis = FavQsConfig.REQUEST_TIMEOUT_MS
            socketTimeoutMillis = FavQsConfig.SOCKET_TIMEOUT_MS
        }

        HttpResponseValidator {
            validateResponse { response ->
                // Only a 401 on a request that actually carried the session token means the token is
                // dead. Login and register run without the header, so a wrong password stays a plain
                // form error instead of tripping the auto-logout.
                val hadSessionToken = response.request.headers.contains(HEADER_USER_TOKEN)
                if (response.status == HttpStatusCode.Unauthorized && hadSessionToken) {
                    sessionExpired.notifyExpired()
                }
            }
        }

        defaultRequest {
            url {
                protocol = URLProtocol.HTTPS
                host = FavQsConfig.BASE_HOST
            }
            header("Authorization", "Token token=\"${FavQsConfig.API_KEY}\"")
            session.userToken?.let { header(HEADER_USER_TOKEN, it) }
        }
    }
