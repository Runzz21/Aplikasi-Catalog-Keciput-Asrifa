package com.keciput.asrifa.ui.pesanan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keciput.asrifa.data.repository.AuthRepository
import com.keciput.asrifa.data.repository.CartRepository
import com.keciput.asrifa.data.repository.OrderHistoryRepository
import com.keciput.asrifa.domain.model.CartItem
import com.keciput.asrifa.domain.model.OrderHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class PesananUiState(
    val cartItems: List<CartItem> = emptyList(),
    val isLoading: Boolean = true,
    val totalPrice: Double = 0.0,
    val lastRemovedItems: List<CartItem> = emptyList(),
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class PesananViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderHistoryRepository: OrderHistoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PesananUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeCart()
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.getCartItems().collect { items ->
                _uiState.update { it.copy(
                    cartItems = items,
                    totalPrice = items.sumOf { it.pricePerUnit * it.quantity },
                    isLoading = false,
                    isLoggedIn = authRepository.isLoggedIn()
                ) }
            }
        }
    }

    fun saveOrderHistory(note: String): Boolean {
        val state = _uiState.value
        if (state.cartItems.isEmpty()) return false

        val userId = authRepository.currentUser?.uid ?: return false

        viewModelScope.launch {
            val order = OrderHistory(
                id = UUID.randomUUID().toString(),
                userId = userId,
                orderDate = System.currentTimeMillis(),
                items = state.cartItems,
                totalPrice = state.totalPrice,
                note = note,
                itemCount = state.cartItems.sumOf { it.quantity }
            )
            orderHistoryRepository.saveOrderHistory(order)
            cartRepository.clearCart()
        }
        return true
    }

    fun updateQuantity(cartItemId: String, quantity: Int) {
        viewModelScope.launch {
            cartRepository.updateQuantity(cartItemId, quantity)
        }
    }

    fun removeItem(item: CartItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(lastRemovedItems = listOf(item)) }
            cartRepository.removeFromCart(item.id)
        }
    }

    fun undoRemove() {
        val items = uiState.value.lastRemovedItems
        if (items.isEmpty()) return

        viewModelScope.launch {
            items.forEach { cartRepository.addToCart(it) }
            _uiState.update { it.copy(lastRemovedItems = emptyList()) }
        }
    }

    fun clearCartWithUndo() {
        viewModelScope.launch {
            val currentItems = uiState.value.cartItems
            _uiState.update { it.copy(lastRemovedItems = currentItems) }
            cartRepository.clearCart()
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart()
        }
    }
}
