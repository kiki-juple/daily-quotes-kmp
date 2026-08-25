package com.disheveled.dailyquotes.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disheveled.dailyquotes.data.repository.FavoritesRepository
import com.disheveled.dailyquotes.data.util.resultOf
import com.disheveled.dailyquotes.domain.model.SavedQuote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val quotes: List<SavedQuote> = emptyList(),
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
        viewModelScope.launch {
            resultOf { favoritesRepository.refreshFavorites() }.onFailure { e ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        actionMessage = e.message ?: "Gagal memuat favorit",
                    )
                }
            }
        }
    }

    fun remove(quoteId: Long) {
        viewModelScope.launch {
            val message = resultOf { favoritesRepository.remove(quoteId) }.fold(
                onSuccess = { "Dihapus dari favorit" },
                onFailure = { e -> e.message ?: "Gagal menghapus favorit" },
            )
            _state.update { it.copy(actionMessage = message) }
        }
    }

    fun consumeActionMessage() {
        _state.update { it.copy(actionMessage = null) }
    }
}
