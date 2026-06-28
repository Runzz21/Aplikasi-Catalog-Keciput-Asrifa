package com.keciput.asrifa.domain.usecase

import com.keciput.asrifa.data.repository.SnackRepository
import com.keciput.asrifa.domain.model.Snack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPopularSnacksUseCase @Inject constructor(
    private val repository: SnackRepository
) {
    operator fun invoke(): Flow<List<Snack>> {
        return repository.getAllSnacks().map { snacks ->
            val popular = snacks.filter { it.isBestseller }
            if (popular.isNotEmpty()) {
                popular.sortedByDescending { it.soldCount }.take(6)
            } else {
                // Fallback: Ambil produk dengan jumlah terjual terbanyak jika belum ada yang di-flag
                snacks.sortedByDescending { it.soldCount }.take(6)
            }
        }
    }
}
