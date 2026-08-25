package com.disheveled.dailyquotes.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disheveled.dailyquotes.data.repository.AuthRepository
import com.disheveled.dailyquotes.data.repository.FavoritesRepository
import com.disheveled.dailyquotes.data.repository.QuoteRepository
import com.disheveled.dailyquotes.data.util.resultOf
import com.disheveled.dailyquotes.domain.model.Quote
import com.disheveled.dailyquotes.domain.model.User
import com.disheveled.dailyquotes.ui.util.todayInIndonesian
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The mutually exclusive states the home screen can be in. Modelled as a sealed hierarchy so the
 * screen cannot land on an unrepresented combination — the previous flat state allowed
 * `quote == null && !isLoading && errorMessage == null`, which rendered a blank screen.
 */
sealed interface HomeContent {
    data object Loading : HomeContent
    data class Error(val message: String) : HomeContent
    data class Loaded(
        val quote: Quote,
        val isFavorite: Boolean,
        val isFavoriteUpdating: Boolean,
    ) : HomeContent
}

data class HomeUiState(
    val content: HomeContent = HomeContent.Loading,
    val user: User? = null,
    /** Formatted here rather than in the composable so it cannot go stale past midnight. */
    val today: String = "",
    val actionMessage: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val quoteRepository: QuoteRepository,
    private val favoritesRepository: FavoritesRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _quoteId = MutableStateFlow<Long?>(null)
    private var refreshJob: Job? = null

    init {
        // Calls refresh() on first load AND whenever the user re-logs in.
        viewModelScope.launch {
            authRepository.currentUser
                .collect { user ->
                    _state.update { it.copy(user = user) }
                    if (user != null) {
                        refresh()
                    } else {
                        _quoteId.value = null
                        refreshJob?.cancel()
                        _state.update { it.copy(content = HomeContent.Loading) }
                    }
                }
        }

        _quoteId
            .flatMapLatest { id ->
                if (id != null) favoritesRepository.observeIsFavorite(id) else flowOf(false)
            }
            .onEach { isFav -> updateQuoteContent { it.copy(isFavorite = isFav) } }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return
        val current = _state.value.content
        if (current !is HomeContent.Loaded) {
            _state.update { it.copy(content = HomeContent.Loading) }
        }
        refreshJob = viewModelScope.launch {
            val result = quoteRepository.getQuoteOfTheDay()
            result.fold(
                onSuccess = { quote ->
                    _state.update {
                        it.copy(
                            content = HomeContent.Loaded(
                                quote = quote,
                                isFavorite = false,
                                isFavoriteUpdating = false,
                            ),
                            // Recomputed with every quote, so the header date and the quote always
                            // describe the same day.
                            today = todayInIndonesian(),
                        )
                    }
                    _quoteId.value = quote.id
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            content = HomeContent.Error(e.message ?: "Gagal memuat kutipan"),
                        )
                    }
                },
            )
        }
    }

    fun toggleFavorite() {
        val content = _state.value.content as? HomeContent.Loaded ?: return
        if (content.isFavoriteUpdating) return
        val quote = content.quote
        val shouldSave = !content.isFavorite

        // Flip the heart immediately; the database flow confirms it, and a failure puts it back.
        updateQuoteContent { it.copy(isFavorite = shouldSave, isFavoriteUpdating = true) }
        viewModelScope.launch {
            val result = resultOf {
                if (shouldSave) favoritesRepository.add(quote) else favoritesRepository.remove(quote.id)
            }
            result.fold(
                onSuccess = {
                    updateQuoteContent { it.copy(isFavoriteUpdating = false) }
                    _state.update {
                        it.copy(
                            actionMessage = if (shouldSave) {
                                "Disimpan ke favorit"
                            } else {
                                "Dihapus dari favorit"
                            },
                        )
                    }
                },
                onFailure = { e ->
                    updateQuoteContent {
                        it.copy(isFavorite = !shouldSave, isFavoriteUpdating = false)
                    }
                    _state.update {
                        it.copy(actionMessage = e.message ?: "Gagal memperbarui favorit")
                    }
                },
            )
        }
    }

    fun consumeActionMessage() {
        _state.update { it.copy(actionMessage = null) }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    private inline fun updateQuoteContent(transform: (HomeContent.Loaded) -> HomeContent.Loaded) {
        _state.update { state ->
            val content = state.content
            if (content is HomeContent.Loaded) state.copy(content = transform(content)) else state
        }
    }
}
