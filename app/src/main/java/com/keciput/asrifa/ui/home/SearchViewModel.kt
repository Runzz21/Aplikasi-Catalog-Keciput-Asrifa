package com.keciput.asrifa.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keciput.asrifa.data.local.UserPreferencesRepository
import com.keciput.asrifa.data.repository.CartRepository
import com.keciput.asrifa.data.repository.SnackRepository
import com.keciput.asrifa.domain.model.Snack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val searchResults: List<Snack> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastRemovedHistory: List<String> = emptyList()
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val snackRepo: SnackRepository,
    private val cartRepo: CartRepository,
    private val userPrefsRepo: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            userPrefsRepo.userDataFlow.collect { userData ->
                _uiState.update { it.copy(searchHistory = userData.searchHistory) }
            }
        }
    }

    fun search(query: String) {
        searchJob?.cancel()
        if (query.length >= 2) {
            searchJob = viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                snackRepo.searchSnacks(query)
                    .catch { e ->
                        _uiState.update { it.copy(isLoading = false, error = e.message) }
                    }
                    .collect { results ->
                        _uiState.update { it.copy(searchResults = results, isLoading = false) }
                        if (results.isNotEmpty()) {
                            userPrefsRepo.addSearchHistory(query)
                        }
                    }
            }
        } else if (query.isEmpty()) {
            _uiState.update { it.copy(searchResults = emptyList(), isLoading = false) }
        }
    }

    fun deleteHistoryItem(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(lastRemovedHistory = listOf(query)) }
            userPrefsRepo.removeSearchHistory(query)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            val currentHistory = uiState.value.searchHistory
            _uiState.update { it.copy(lastRemovedHistory = currentHistory) }
            userPrefsRepo.clearSearchHistory()
        }
    }

    fun undoHistoryRemoval() {
        val items = uiState.value.lastRemovedHistory
        if (items.isEmpty()) return
        
        viewModelScope.launch {
            items.reversed().forEach { 
                userPrefsRepo.addSearchHistory(it)
            }
            _uiState.update { it.copy(lastRemovedHistory = emptyList()) }
        }
    }

    fun restoreHistoryItem(query: String) {
        viewModelScope.launch {
            userPrefsRepo.addSearchHistory(query)
        }
    }

    fun restoreHistoryList(queries: List<String>) {
        viewModelScope.launch {
            queries.reversed().forEach { 
                userPrefsRepo.addSearchHistory(it)
            }
        }
    }

    fun addToCart(snack: Snack) {
        viewModelScope.launch {
            cartRepo.addToCart(snack.toCartItem())
        }
    }
}
