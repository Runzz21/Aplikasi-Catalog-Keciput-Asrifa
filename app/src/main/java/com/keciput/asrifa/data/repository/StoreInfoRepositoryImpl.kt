package com.keciput.asrifa.data.repository

import com.keciput.asrifa.data.local.dao.StoreInfoDao
import com.keciput.asrifa.data.local.entity.asEntity
import com.keciput.asrifa.domain.model.StoreInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreInfoRepositoryImpl @Inject constructor(
    private val storeInfoDao: StoreInfoDao
) : StoreInfoRepository {

    override fun getStoreInfo(): Flow<StoreInfo?> {
        return storeInfoDao.getStoreInfo().map { it?.asDomainModel() }
    }

    override suspend fun saveStoreInfo(storeInfo: StoreInfo) {
        storeInfoDao.insertStoreInfo(storeInfo.asEntity())
    }
}
