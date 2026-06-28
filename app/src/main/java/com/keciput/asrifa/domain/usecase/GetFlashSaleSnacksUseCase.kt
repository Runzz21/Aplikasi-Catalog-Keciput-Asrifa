package com.keciput.asrifa.domain.usecase

import com.keciput.asrifa.data.repository.SnackRepository
import com.keciput.asrifa.domain.model.Snack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetFlashSaleSnacksUseCase @Inject constructor(
    private val repository: SnackRepository
) {
    operator fun invoke(): Flow<List<Snack>> {
        return repository.getAllSnacks().map { snacks ->
            val flashSales = snacks.filter { it.isFlashSale }
            if (flashSales.isNotEmpty()) {
                flashSales.take(6)
            } else {
                // Fallback: Jika belum ada flag, ambil 6 produk acak
                snacks.shuffled().take(6).map { it.copy(isFlashSale = true) }
            }
        }
    }
}
