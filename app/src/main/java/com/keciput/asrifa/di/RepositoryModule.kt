package com.keciput.asrifa.di

import com.keciput.asrifa.data.repository.AuthRepository
import com.keciput.asrifa.data.repository.AuthRepositoryImpl
import com.keciput.asrifa.data.repository.CartRepository
import com.keciput.asrifa.data.repository.CartRepositoryImpl
import com.keciput.asrifa.data.repository.CategoryRepository
import com.keciput.asrifa.data.repository.CategoryRepositoryImpl
import com.keciput.asrifa.data.repository.OrderHistoryRepository
import com.keciput.asrifa.data.repository.OrderHistoryRepositoryImpl
import com.keciput.asrifa.data.repository.ReviewRepository
import com.keciput.asrifa.data.repository.ReviewRepositoryImpl
import com.keciput.asrifa.data.repository.SnackRepository
import com.keciput.asrifa.data.repository.SnackRepositoryImpl
import com.keciput.asrifa.data.repository.StoreInfoRepository
import com.keciput.asrifa.data.repository.StoreInfoRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSnackRepository(
        snackRepositoryImpl: SnackRepositoryImpl
    ): SnackRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        cartRepositoryImpl: CartRepositoryImpl
    ): CartRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(
        reviewRepositoryImpl: ReviewRepositoryImpl
    ): ReviewRepository

    @Binds
    @Singleton
    abstract fun bindStoreInfoRepository(
        storeInfoRepositoryImpl: StoreInfoRepositoryImpl
    ): StoreInfoRepository

    @Binds
    @Singleton
    abstract fun bindOrderHistoryRepository(
        orderHistoryRepositoryImpl: OrderHistoryRepositoryImpl
    ): OrderHistoryRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}
