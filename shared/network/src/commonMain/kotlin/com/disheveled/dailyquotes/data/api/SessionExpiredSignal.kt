package com.disheveled.dailyquotes.data.api

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Broadcasts "the server rejected our session token".
 *
 * Lives in the network module so the HTTP layer can report a dead token without depending on the
 * data layer: `AuthRepository` observes [events] and logs out. There is no replay — a subscriber
 * that attaches later must not be handed a stale expiry and log the user straight back out.
 */
class SessionExpiredSignal {

    private val _events = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val events: SharedFlow<Unit> = _events.asSharedFlow()

    fun notifyExpired() {
        _events.tryEmit(Unit)
    }
}
