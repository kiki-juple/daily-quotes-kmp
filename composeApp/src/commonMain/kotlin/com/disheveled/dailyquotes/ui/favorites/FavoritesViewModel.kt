package com.disheveled.dailyquotes.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disheveled.dailyquotes.data.repository.FavoritesRepository
import com.disheveled.dailyquotes.domain.model.Quote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val quotes: List<Quote> = emptyList(),
    val isLoading: Boolean = true,
    val actionMessage: String? = null,
)

class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesUiState())
    val state: StateFlow<FavoritesUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepository.observeFavorites().collect { quotes ->
                _state.update { it.copy(quotes = quotes, isLoading = false) }
            }
        }
    }

    fun remove(quoteId: Long) {
        viewModelScope.launch {
            try {
                favoritesRepository.remove(quoteId)
                _state.update { it.copy(actionMessage = "Dihapus dari favorit") }
            } catch (e: Exception) {
                _state.update {
                    it.copy(actionMessage = e.message ?: "Gagal menghapus favorit")
                }
            }
        }
    }

    fun consumeActionMessage() {
        _state.update { it.copy(actionMessage = null) }
    }
}
