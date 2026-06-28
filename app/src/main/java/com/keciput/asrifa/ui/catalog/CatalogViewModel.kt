package com.keciput.asrifa.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keciput.asrifa.data.repository.CartRepository
import com.keciput.asrifa.data.repository.CategoryRepository
import com.keciput.asrifa.data.repository.SnackRepository
import com.keciput.asrifa.domain.model.Snack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ViewMode { GRID, LIST }

enum class SortOption(val label: String) {
    POPULER("Populer"),
    TERMURAH("Termurah"),
    RATING("Rating"),
    TERBARU("Terbaru")
}

sealed interface CatalogDataState {
    object Loading : CatalogDataState
    data class Success(val snacks: List<Snack>) : CatalogDataState
    data class Error(val message: String) : CatalogDataState
    object Empty : CatalogDataState
}

data class CatalogUiState(
    val dataState: CatalogDataState = CatalogDataState.Loading,
    val categories: List<String> = listOf(
        "Semua", 
        "Kerupuk & Rambak", 
        "Snack Ringan", 
        "Kacang-kacangan", 
        "Olahan Singkong & Pisang", 
        "Olahan Tempe", 
        "Kue Kering", 
        "Olahan Tahu"
    ),
    val searchQuery: String = "",
    val selectedCategory: String = "Semua",
    val sortOption: SortOption = SortOption.POPULER,
    val viewMode: ViewMode = ViewMode.GRID,
    val filterType: String? = null,
    val searchHistory: List<String> = emptyList(),
    val lastRemovedHistory: String? = null
)

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val snackRepo: SnackRepository,
    private val categoryRepo: CategoryRepository,
    private val cartRepo: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("Semua")
    private val _selectedSort = MutableStateFlow(SortOption.POPULER)
    private val _filterType = MutableStateFlow<String?>(null)

    init {
        observeSnacks()
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepo.getAllCategories().collect { categories ->
                if (categories.isNotEmpty()) {
                    val categoryNames = listOf("Semua") + categories.map { it.name }
                    _uiState.update { it.copy(categories = categoryNames) }
                }
            }
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeSnacks() {
        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300),
                _selectedCategory,
                _selectedSort,
                _filterType
            ) { query, category, sort, filterType ->
                _uiState.update { 
                    it.copy(dataState = CatalogDataState.Loading) 
                }
                FilterParams(query, category, sort, filterType)
            }.flatMapLatest { params ->
                snackRepo.getSnacksByFilter(if (params.category == "Semua") null else params.category)
                    .map { list ->
                        var filteredList = list
                        
                        // Apply filterType if present
                        if (params.filterType != null) {
                            filteredList = when (params.filterType) {
                                "featured" -> filteredList.filter { it.isFeatured }
                                "popular" -> filteredList.filter { it.isBestseller }
                                else -> filteredList
                            }
                        }

                        if (params.query.isNotBlank()) {
                            filteredList = filteredList.filter { 
                                it.name.contains(params.query, ignoreCase = true) || 
                                it.category.contains(params.query, ignoreCase = true) 
                            }
                        }
                        
                        when (params.sort) {
                            SortOption.POPULER -> filteredList.sortedByDescending { it.soldCount }
                            SortOption.TERMURAH -> filteredList.sortedBy { it.price }
                            SortOption.RATING -> filteredList.sortedByDescending { it.rating }
                            SortOption.TERBARU -> filteredList.sortedByDescending { it.id }
                        }
                    }
            }.collect { snacks ->
                _uiState.update { 
                    it.copy(
                        dataState = if (snacks.isEmpty()) CatalogDataState.Empty else CatalogDataState.Success(snacks)
                    ) 
                }
            }
        }
    }

    private data class FilterParams(
        val query: String,
        val category: String,
        val sort: SortOption,
        val filterType: String?
    )

    fun onSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onSearchSubmit(query: String) {
        if (query.isBlank()) return
        _uiState.update { state ->
            val updated = (listOf(query) + state.searchHistory.filter { it != query }).take(6)
            state.copy(searchHistory = updated)
        }
    }

    fun removeSearchHistory(query: String) {
        _uiState.update { state ->
            state.copy(
                searchHistory = state.searchHistory.filter { it != query },
                lastRemovedHistory = query
            )
        }
    }

    fun undoRemoveSearchHistory() {
        _uiState.update { state ->
            val restored = state.lastRemovedHistory ?: return@update state
            val updated = (listOf(restored) + state.searchHistory).take(6)
            state.copy(searchHistory = updated, lastRemovedHistory = null)
        }
    }

    fun clearSearchHistory() {
        _uiState.update { state ->
            state.copy(
                searchHistory = emptyList(),
                lastRemovedHistory = null
            )
        }
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onFilterTypeSelected(filterType: String?) {
        _filterType.value = filterType
        _uiState.update { it.copy(filterType = filterType) }
    }

    fun onSortChange(sort: SortOption) {
        _selectedSort.value = sort
        _uiState.update { it.copy(sortOption = sort) }
    }

    fun onViewToggle() {
        _uiState.update {
            it.copy(
                viewMode = if (it.viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
            )
        }
    }

    fun addToCart(snack: Snack) {
        viewModelScope.launch {
            cartRepo.addToCart(snack.toCartItem())
        }
    }

    fun onResetFilter() {
        onSearchQuery("")
        onCategorySelected("Semua")
        onFilterTypeSelected(null)
        onSortChange(SortOption.POPULER)
    }
}
