package com.disheveled.dailyquotes.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disheveled.dailyquotes.data.repository.AuthRepository
import com.disheveled.dailyquotes.data.repository.FavoritesRepository
import com.disheveled.dailyquotes.data.repository.QuoteRepository
import com.disheveled.dailyquotes.domain.model.Quote
import com.disheveled.dailyquotes.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val quote: Quote? = null,
    val isFavorite: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
)

class HomeViewModel(
    private val quoteRepository: QuoteRepository,
    private val favoritesRepository: FavoritesRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _state.update { it.copy(user = user) }
            }
        }
        refresh()
    }

    fun refresh() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = quoteRepository.getQuoteOfTheDay()
            result.fold(
                onSuccess = { quote ->
                    _state.update { it.copy(isLoading = false, quote = quote) }
                    observeFavorite(quote.id)
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

    private var observeJob: kotlinx.coroutines.Job? = null

    private fun observeFavorite(quoteId: Long) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            favoritesRepository.observeIsFavorite(quoteId).collect { isFav ->
                _state.update { it.copy(isFavorite = isFav) }
            }
        }
    }

    fun toggleFavorite() {
        val quote = _state.value.quote ?: return
        val currentlyFavorite = _state.value.isFavorite
        viewModelScope.launch {
            if (currentlyFavorite) {
                favoritesRepository.remove(quote.id)
            } else {
                favoritesRepository.add(quote)
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
