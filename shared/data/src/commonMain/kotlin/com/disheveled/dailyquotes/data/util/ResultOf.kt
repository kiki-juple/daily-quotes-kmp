package com.disheveled.dailyquotes.data.util

import kotlinx.coroutines.CancellationException

/**
 * Runs [block] and wraps the outcome in [Result], rethrowing [CancellationException] so that
 * coroutine cancellation continues to unwind correctly.
 */
internal inline fun <T> resultOf(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (c: CancellationException) {
    throw c
} catch (t: Throwable) {
    Result.failure(t)
}
