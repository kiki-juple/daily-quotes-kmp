package com.disheveled.dailyquotes.data.util

import kotlinx.coroutines.CancellationException

/**
 * Runs [block] and wraps the outcome in [Result], rethrowing [CancellationException] so that
 * coroutine cancellation continues to unwind correctly.
 *
 * Public because ViewModels need it too: a bare `catch (e: Exception)` swallows
 * [CancellationException] (it is an `Exception` on the JVM) and breaks structured concurrency.
 */
inline fun <T> resultOf(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (c: CancellationException) {
    throw c
} catch (t: Throwable) {
    Result.failure(t)
}
