package com.disheveled.dailyquotes.data.api

import com.disheveled.dailyquotes.data.api.dto.ApiErrorDto
import com.disheveled.dailyquotes.data.api.dto.CreateSessionBody
import com.disheveled.dailyquotes.data.api.dto.CreateSessionRequest
import com.disheveled.dailyquotes.data.api.dto.QuoteOfTheDayDto
import com.disheveled.dailyquotes.data.api.dto.RegisterUserBody
import com.disheveled.dailyquotes.data.api.dto.RegisterUserRequest
import com.disheveled.dailyquotes.data.api.dto.SessionDto
import com.disheveled.dailyquotes.data.api.dto.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.path
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class FavQsApi(private val client: HttpClient) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    suspend fun getQuoteOfTheDay(): QuoteOfTheDayDto = call {
        val response = client.get { url { path(FavQsConfig.BASE_PATH, "qotd") } }
        parseOrThrow<QuoteOfTheDayDto>(response)
    }

    suspend fun register(
        login: String,
        email: String,
        password: String,
    ): UserDto = call {
        val response = client.post {
            url { path(FavQsConfig.BASE_PATH, "users") }
            contentType(ContentType.Application.Json)
            setBody(
                RegisterUserRequest(
                    RegisterUserBody(
                        login = login,
                        email = email,
                        password = password,
                        passwordConfirmation = password,
                    )
                )
            )
        }
        parseOrThrow<UserDto>(response)
    }

    suspend fun createSession(login: String, password: String): SessionDto = call {
        require(login.isNotBlank()) { "Username wajib diisi" }
        require(password.isNotBlank()) { "Sandi wajib diisi" }
        val response = client.post {
            url { path(FavQsConfig.BASE_PATH, "session") }
            contentType(ContentType.Application.Json)
            setBody(CreateSessionRequest(CreateSessionBody(login.trim(), password)))
        }
        parseOrThrow<SessionDto>(response)
    }

    suspend fun getUser(login: String): UserDto = call {
        require(login.isNotBlank()) { "Username wajib diisi" }
        require(!login.contains("@")) { "Gunakan username FavQs, bukan email" }
        val response = client.get {
            url { path(FavQsConfig.BASE_PATH, "users", login) }
        }
        if (response.status == HttpStatusCode.NotFound) {
            throw ApiException.NotFound("Pengguna \"$login\" tidak ditemukan")
        }
        parseOrThrow<UserDto>(response)
    }

    private suspend inline fun <reified T> parseOrThrow(response: HttpResponse): T {
        val raw = response.bodyAsText()
        if (!response.status.value.isSuccess()) {
            throw apiErrorOf(raw, response.status)
        }
        runCatching { json.decodeFromString<JsonObject>(raw) }
            .getOrNull()
            ?.let { root ->
                val looksLikeError = root.containsKey("error_code") ||
                    (root.containsKey("message") &&
                        !root.containsKey("login") &&
                        !root.containsKey("Login") &&
                        !root.containsKey("User-Token") &&
                        !root.containsKey("quote") &&
                        !root.containsKey("qotd_date"))
                if (looksLikeError) {
                    throw apiErrorOf(raw, response.status)
                }
            }
        return try {
            json.decodeFromString<T>(raw)
        } catch (e: SerializationException) {
            throw ApiException.ParseError(e)
        }
    }

    private fun apiErrorOf(text: String, status: HttpStatusCode): ApiException {
        if (status == HttpStatusCode.Unauthorized) {
            return ApiException.Unauthorized()
        }
        val parsed = runCatching { json.decodeFromString<ApiErrorDto>(text) }.getOrNull()
        val message = parsed?.message ?: defaultMessage(text, status)
        return ApiException.ApiError(message, errorCode = parsed?.errorCode)
    }

    private fun defaultMessage(text: String, status: HttpStatusCode): String {
        val trimmed = text.take(200)
        return if (trimmed.isBlank()) {
            "Permintaan gagal (${status.value})"
        } else {
            "Permintaan gagal (${status.value}): $trimmed"
        }
    }

    private fun Int.isSuccess(): Boolean = this in 200..299

    private suspend inline fun <T> call(block: () -> T): T = try {
        block()
    } catch (e: ApiException) {
        throw e
    } catch (e: HttpRequestTimeoutException) {
        throw ApiException.NetworkError(e)
    } catch (e: SocketTimeoutException) {
        throw ApiException.NetworkError(e)
    } catch (e: IOException) {
        throw ApiException.NetworkError(e)
    }
}
