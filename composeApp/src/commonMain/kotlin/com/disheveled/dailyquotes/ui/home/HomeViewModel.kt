package com.disheveled.dailyquotes.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disheveled.dailyquotes.data.repository.AuthRepository
import com.disheveled.dailyquotes.data.repository.FavoritesRepository
import com.disheveled.dailyquotes.data.repository.QuoteRepository
import com.disheveled.dailyquotes.domain.model.Quote
import com.disheveled.dailyquotes.domain.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val quote: Quote? = null,
    val isFavorite: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
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

    init {
        // Calls refresh() on first load AND whenever the user re-logs in.
        // This also handles the case where the ViewModel is retained across logout/login:
        // without this, _quoteId would stay null after logout and isFavorite would be stuck false.
        viewModelScope.launch {
            authRepository.currentUser
                .collect { user ->
                    _state.update { it.copy(user = user) }
                    if (user != null) refresh()
                }
        }

        _quoteId
            .flatMapLatest { id ->
                if (id != null) favoritesRepository.observeIsFavorite(id) else flowOf(false)
            }
            .onEach { isFav -> _state.update { it.copy(isFavorite = isFav) } }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = quoteRepository.getQuoteOfTheDay()
            result.fold(
                onSuccess = { quote ->
                    _state.update { it.copy(isLoading = false, quote = quote) }
                    _quoteId.value = quote.id
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Gagal memuat kutipan",
                        )
                    }
                },
            )
        }
    }

    fun toggleFavorite() {
        val quote = _state.value.quote ?: return
        val currentlyFavorite = _state.value.isFavorite
        viewModelScope.launch {
            try {
                if (currentlyFavorite) {
                    favoritesRepository.remove(quote.id)
                } else {
                    favoritesRepository.add(quote)
                }
            } catch (_: Exception) {
                // isFavorite reverts automatically via observeIsFavorite flow
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
