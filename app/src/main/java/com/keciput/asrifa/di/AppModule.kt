package com.keciput.asrifa.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.google.gson.Gson
import com.keciput.asrifa.data.local.KeciputDatabase
import com.keciput.asrifa.data.local.dao.CartDao
import com.keciput.asrifa.data.local.dao.CategoryDao
import com.keciput.asrifa.data.local.dao.OrderHistoryDao
import com.keciput.asrifa.data.local.dao.ReviewDao
import com.keciput.asrifa.data.local.dao.SnackDao
import com.keciput.asrifa.data.local.dao.StoreInfoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KeciputDatabase {
        return Room.databaseBuilder(
            context,
            KeciputDatabase::class.java,
            "keciput_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideSnackDao(db: KeciputDatabase): SnackDao = db.snackDao()

    @Provides
    fun provideCategoryDao(db: KeciputDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideCartDao(db: KeciputDatabase): CartDao = db.cartDao()

    @Provides
    fun provideReviewDao(db: KeciputDatabase): ReviewDao = db.reviewDao()

    @Provides
    fun provideStoreInfoDao(db: KeciputDatabase): StoreInfoDao = db.storeInfoDao()

    @Provides
    fun provideOrderHistoryDao(db: KeciputDatabase): OrderHistoryDao = db.orderHistoryDao()

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("user_prefs") }
        )
    }
}
