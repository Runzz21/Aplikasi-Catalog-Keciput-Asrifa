package com.keciput.asrifa.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keciput.asrifa.data.repository.AuthRepository
import com.keciput.asrifa.data.repository.CartRepository
import com.keciput.asrifa.data.repository.ReviewRepository
import com.keciput.asrifa.data.repository.SnackRepository
import com.keciput.asrifa.domain.model.PackagingType
import com.keciput.asrifa.domain.model.Review
import com.keciput.asrifa.domain.model.Snack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val snack: Snack? = null,
    val relatedSnacks: List<Snack> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val selectedPackaging: PackagingType = PackagingType.ECERAN,
    val selectedVariant: String? = null,
    val quantity: Int = 1,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val snackRepo: SnackRepository,
    private val cartRepo: CartRepository,
    private val reviewRepo: ReviewRepository,
    private val authRepo: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val snackId: Int = checkNotNull(savedStateHandle["snackId"])

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(isLoggedIn = authRepo.isLoggedIn()) }
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val snack = snackRepo.getSnackById(snackId)
            if (snack != null) {
                _uiState.update { it.copy(
                    snack = snack, 
                    selectedVariant = snack.variants.firstOrNull(),
                    isLoading = false 
                ) }
                
                // Load Reviews
                launch {
                    reviewRepo.getReviewsForSnack(snackId).collect { reviews ->
                        _uiState.update { it.copy(reviews = reviews) }
                    }
                }

                // Load Related Snacks
                launch {
                    snackRepo.getSnacksByFilter(snack.category).collect { allSnacks ->
                        val related = allSnacks.filter { it.id != snackId }.take(8)
                        _uiState.update { it.copy(relatedSnacks = related) }
                    }
                }
            } else {
                _uiState.update { it.copy(error = "Snack tidak ditemukan", isLoading = false) }
            }
        }
    }

    fun onPackagingSelected(type: PackagingType) {
        _uiState.update { it.copy(selectedPackaging = type) }
    }

    fun onVariantSelected(variant: String) {
        _uiState.update { it.copy(selectedVariant = variant) }
    }

    fun updateQuantity(newQuantity: Int) {
        if (newQuantity >= 1) {
            _uiState.update { it.copy(quantity = newQuantity) }
        }
    }

    fun addToCart() {
        val snack = uiState.value.snack ?: return
        viewModelScope.launch {
            cartRepo.addToCart(
                snack.toCartItem(
                    quantity = uiState.value.quantity,
                    packagingType = uiState.value.selectedPackaging,
                    selectedVariant = uiState.value.selectedVariant
                )
            )
        }
    }
}
