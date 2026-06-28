package com.keciput.asrifa.ui.riwayat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keciput.asrifa.data.repository.AuthRepository
import com.keciput.asrifa.data.repository.OrderHistoryRepository
import com.keciput.asrifa.domain.model.OrderHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RiwayatUiState(
    val orders: List<OrderHistory> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class RiwayatPesananViewModel @Inject constructor(
    private val orderHistoryRepository: OrderHistoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RiwayatUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeOrders()
    }

    private fun observeOrders() {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch
            orderHistoryRepository.getOrderHistoryByUser(userId).collect { orders ->
                _uiState.update { it.copy(orders = orders, isLoading = false) }
            }
        }
    }
}
