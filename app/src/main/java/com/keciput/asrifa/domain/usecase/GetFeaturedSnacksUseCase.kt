package com.keciput.asrifa.domain.usecase

import com.keciput.asrifa.data.repository.SnackRepository
import com.keciput.asrifa.domain.model.Snack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetFeaturedSnacksUseCase @Inject constructor(
    private val repository: SnackRepository
) {
    operator fun invoke(): Flow<List<Snack>> {
        return repository.getAllSnacks().map { snacks ->
            val featured = snacks.filter { it.isFeatured }
            if (featured.isNotEmpty()) {
                featured.take(6)
            } else {
                // Fallback: Ambil 6 produk dengan rating tertinggi jika belum ada yang di-flag
                snacks.sortedByDescending { it.rating }.take(6)
            }
        }
    }
}
