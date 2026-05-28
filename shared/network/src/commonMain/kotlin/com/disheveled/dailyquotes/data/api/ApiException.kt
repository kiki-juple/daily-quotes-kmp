package com.disheveled.dailyquotes.data.api

sealed class ApiException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {

    class NetworkError(cause: Throwable) :
        ApiException("Tidak terhubung ke jaringan", cause)

    class ApiError(message: String, val errorCode: Int? = null) :
        ApiException(message)

    class Unauthorized(message: String = "Sesi berakhir. Masuk lagi.") :
        ApiException(message)

    class NotFound(message: String) :
        ApiException(message)

    class ParseError(cause: Throwable) :
        ApiException("Gagal memproses respons", cause)
}
