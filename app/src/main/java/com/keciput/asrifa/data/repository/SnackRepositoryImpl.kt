package com.keciput.asrifa.data.repository

import com.keciput.asrifa.data.local.dao.SnackDao
import com.keciput.asrifa.domain.model.Snack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SnackRepositoryImpl @Inject constructor(
    private val snackDao: SnackDao
) : SnackRepository {

    override fun getAllSnacks(): Flow<List<Snack>> {
        return snackDao.getAllSnacks().map { snacks ->
            snacks.map { it.asDomainModel() }
        }
    }

    override fun getTopRatedSnacks(): Flow<List<Snack>> {
        return snackDao.getTopRatedSnacks().map { snacks ->
            snacks.map { it.asDomainModel() }
        }
    }

    override fun getSnacksByFilter(category: String?): Flow<List<Snack>> {
        return snackDao.getSnacksByFilter(category).map { snacks ->
            snacks.map { it.asDomainModel() }
        }
    }

    override fun searchSnacks(query: String): Flow<List<Snack>> {
        return snackDao.searchSnacks(query).map { snacks ->
            snacks.map { it.asDomainModel() }
        }
    }

    override suspend fun getSnackById(id: Int): Snack? {
        return snackDao.getSnackById(id)?.asDomainModel()
    }
}
