package com.keciput.asrifa.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.keciput.asrifa.data.local.dao.CartDao
import com.keciput.asrifa.data.local.dao.CategoryDao
import com.keciput.asrifa.data.local.dao.OrderHistoryDao
import com.keciput.asrifa.data.local.dao.ReviewDao
import com.keciput.asrifa.data.local.dao.SnackDao
import com.keciput.asrifa.data.local.dao.StoreInfoDao
import com.keciput.asrifa.data.local.entity.CartEntity
import com.keciput.asrifa.data.local.entity.CategoryEntity
import com.keciput.asrifa.data.local.entity.OrderHistoryEntity
import com.keciput.asrifa.data.local.entity.ReviewEntity
import com.keciput.asrifa.data.local.entity.SnackEntity
import com.keciput.asrifa.data.local.entity.StoreInfoEntity

@Database(
    entities = [
        SnackEntity::class,
        CategoryEntity::class,
        CartEntity::class,
        ReviewEntity::class,
        StoreInfoEntity::class,
        OrderHistoryEntity::class
    ],
    version = 7,
    exportSchema = true
)
abstract class KeciputDatabase : RoomDatabase() {
    abstract fun snackDao(): SnackDao
    abstract fun categoryDao(): CategoryDao
    abstract fun cartDao(): CartDao
    abstract fun reviewDao(): ReviewDao
    abstract fun storeInfoDao(): StoreInfoDao
    abstract fun orderHistoryDao(): OrderHistoryDao

    companion object {
        // Mulai dari sini, migrasi dilakukan proper (tidak destructive)
    }
}
