package com.keciput.asrifa.data.local

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.keciput.asrifa.data.local.entity.CategoryEntity
import com.keciput.asrifa.data.local.entity.ReviewEntity
import com.keciput.asrifa.data.local.entity.SnackEntity
import com.keciput.asrifa.data.local.entity.StoreInfoEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class DatabaseSeeder @Inject constructor(
    private val db: KeciputDatabase,
    @ApplicationContext private val context: Context
) {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)

    suspend fun seedIfEmpty() {
        Log.e("DATABASE_SEEDER", "=== MEMULAI PROSES UPDATE (IO THREAD) ===")
        
        withContext(Dispatchers.IO) {
            try {
                // 1. Seed Store Info
                val currentStoreInfo = db.storeInfoDao().getStoreInfo().firstOrNull()
                if (currentStoreInfo == null) {
                    val defaultStoreInfo = StoreInfoEntity(
                        openTime = "08:00",
                        closeTime = "17:00",
                        operatingDays = "1,2,3,4,5,6",
                        holidayDates = "",
                        holidayMessage = "Toko sedang libur hari raya",
                        isNotificationEnabled = true
                    )
                    db.storeInfoDao().insertStoreInfo(defaultStoreInfo)
                }

                // 2. Baca and Update Snacks
                val snackJson = context.assets.open("snacks.json").bufferedReader().use { it.readText() }
                val snacks = Gson().fromJson<List<SnackEntity>>(snackJson, object : TypeToken<List<SnackEntity>>() {}.type)
                db.snackDao().deleteAllSnacks()
                db.snackDao().upsertSnacks(snacks)

                // 3. Baca and Update Categories
                val catJson = context.assets.open("categories.json").bufferedReader().use { it.readText() }
                val categories = Gson().fromJson<List<CategoryEntity>>(catJson, object : TypeToken<List<CategoryEntity>>() {}.type)
                db.categoryDao().upsertCategories(categories)

                // 4. Baca and Update Reviews (BARU)
                val reviewJson = context.assets.open("reviews.json").bufferedReader().use { it.readText() }
                val rawReviews = Gson().fromJson<List<Map<String, Any>>>(reviewJson, object : TypeToken<List<Map<String, Any>>>() {}.type)
                
                val reviewEntities = rawReviews.map { raw ->
                    ReviewEntity(
                        id = raw["id"] as String,
                        snackId = (raw["snackId"] as String).toInt(),
                        userName = raw["reviewerName"] as String,
                        rating = (raw["rating"] as Double).toInt(),
                        comment = raw["comment"] as String,
                        reviewDate = try {
                            dateFormat.parse(raw["reviewDate"] as String)?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }
                    )
                }
                
                // Clear and insert reviews to ensure sync with assets
                db.reviewDao().upsertReviews(reviewEntities)
                Log.e("DATABASE_SEEDER", "BERHASIL MEMASUKKAN ${reviewEntities.size} DATA ULASAN")
                
            } catch (e: Exception) {
                Log.e("DATABASE_SEEDER", "!!! GAGAL UPDATE: ${e.message}")
                e.printStackTrace()
            }
        }
        Log.e("DATABASE_SEEDER", "=== PROSES UPDATE SELESAI ===")
    }
}
