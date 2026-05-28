package com.disheveled.dailyquotes.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object FavQsConfig {
    const val BASE_HOST: String = "favqs.com"
    const val BASE_PATH: String = "/api"
    // Sourced from local.properties (favqs.api.key) or env FAVQS_API_KEY via :shared:network generateApiKey task.
    val API_KEY: String get() = FAVQS_API_KEY
}

fun createHttpClient(engine: HttpClientEngine, session: SessionStore): HttpClient =
    HttpClient(engine) {
        expectSuccess = false

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                }
            )
        }

        install(Logging) {
            // Default off in shared code; flip to LogLevel.BODY locally when debugging requests.
            level = LogLevel.NONE
            logger = object : Logger {
                override fun log(message: String) {
                    println("HTTP: $message")
                }
            }
        }

        defaultRequest {
            url {
                protocol = URLProtocol.HTTPS
                host = FavQsConfig.BASE_HOST
            }
            header("Authorization", "Token token=\"${FavQsConfig.API_KEY}\"")
            session.userToken?.let { header("User-Token", it) }
        }
    }
