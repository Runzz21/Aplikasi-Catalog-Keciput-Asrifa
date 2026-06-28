package com.keciput.asrifa.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keciput.asrifa.data.AdminAuthHelper
import com.keciput.asrifa.data.remote.FirestoreDataSource
import com.keciput.asrifa.domain.model.Snack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val snacks: List<Snack> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isAdmin: Boolean = false
)

data class AdminFormState(
    val id: Int = 0,
    val name: String = "",
    val price: String = "",
    val category: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val rating: String = "5.0",
    val weight: String = "250g",
    val isFeatured: Boolean = false,
    val isBestseller: Boolean = false,
    val isFlashSale: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val firestore: FirestoreDataSource,
    private val adminAuth: AdminAuthHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(AdminFormState())
    val formState: StateFlow<AdminFormState> = _formState.asStateFlow()

    init {
        checkAdminAndLoad()
    }

    private fun checkAdminAndLoad() {
        viewModelScope.launch {
            val isAdmin = adminAuth.isAdmin()
            _uiState.update { it.copy(isAdmin = isAdmin) }
            if (isAdmin) loadSnacks()
        }
    }

    fun loadSnacks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val snacks = firestore.getAllSnacks()
                _uiState.update { it.copy(snacks = snacks, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun startAdd() {
        _formState.value = AdminFormState()
    }

    fun startEdit(snack: Snack) {
        _formState.value = AdminFormState(
            id = snack.id, name = snack.name,
            price = snack.price.toLong().toString(),
            category = snack.category, description = snack.description,
            imageUrl = snack.imageUrl, rating = snack.rating.toString(),
            weight = snack.weightDefault, isFeatured = snack.isFeatured,
            isBestseller = snack.isBestseller, isFlashSale = snack.isFlashSale
        )
    }

    fun updateForm(transform: (AdminFormState) -> AdminFormState) {
        _formState.update(transform)
    }

    fun saveSnack() {
        val f = _formState.value
        if (f.name.isBlank() || f.price.isBlank()) {
            _formState.update { it.copy(error = "Nama dan harga harus diisi") }
            return
        }
        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true, error = null) }
            try {
                val price = f.price.toDoubleOrNull() ?: throw Exception("Harga tidak valid")
                val id = if (f.id == 0) firestore.getNextSnackId() else f.id
                val snack = Snack(
                    id = id, name = f.name, price = price,
                    category = f.category, description = f.description,
                    imageUrl = f.imageUrl, rating = f.rating.toDoubleOrNull() ?: 5.0,
                    weightDefault = f.weight, isFeatured = f.isFeatured,
                    isBestseller = f.isBestseller, isFlashSale = f.isFlashSale
                )
                firestore.addSnack(snack)
                _formState.value = AdminFormState()
                loadSnacks()
            } catch (e: Exception) {
                _formState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun deleteSnack(snackId: Int) {
        viewModelScope.launch {
            try {
                firestore.deleteSnack(snackId)
                loadSnacks()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
