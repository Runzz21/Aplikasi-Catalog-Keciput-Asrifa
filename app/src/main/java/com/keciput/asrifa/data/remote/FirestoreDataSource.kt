package com.keciput.asrifa.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.keciput.asrifa.domain.model.PackagingType
import com.keciput.asrifa.domain.model.Snack
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class FirestoreSnack(
    val id: Int = 0,
    val name: String = "",
    val imageUrl: String = "",
    val price: Double = 0.0,
    val originalPrice: Double? = null,
    val rating: Double = 0.0,
    val category: String = "",
    val description: String = "",
    val reviewCount: Int = 0,
    val weightDefault: String = "250g",
    val variants: List<String> = emptyList(),
    val hasBulkPackaging: Boolean = false,
    val isBestseller: Boolean = false,
    val isNew: Boolean = false,
    val isHot: Boolean = false,
    val isFeatured: Boolean = false,
    val isFlashSale: Boolean = false,
    val soldCount: Int = 0
) {
    fun toDomain() = Snack(
        id = id, name = name, imageUrl = imageUrl, price = price,
        originalPrice = originalPrice, rating = rating, category = category,
        description = description, reviewCount = reviewCount,
        weightDefault = weightDefault, variants = variants,
        hasBulkPackaging = hasBulkPackaging, isBestseller = isBestseller,
        isNew = isNew, isHot = isHot, isFeatured = isFeatured,
        isFlashSale = isFlashSale, soldCount = soldCount
    )

    companion object {
        fun fromDomain(snack: Snack) = FirestoreSnack(
            id = snack.id, name = snack.name, imageUrl = snack.imageUrl,
            price = snack.price, originalPrice = snack.originalPrice,
            rating = snack.rating, category = snack.category,
            description = snack.description, reviewCount = snack.reviewCount,
            weightDefault = snack.weightDefault, variants = snack.variants,
            hasBulkPackaging = snack.hasBulkPackaging,
            isBestseller = snack.isBestseller, isNew = snack.isNew,
            isHot = snack.isHot, isFeatured = snack.isFeatured,
            isFlashSale = snack.isFlashSale, soldCount = snack.soldCount
        )
    }
}

@Singleton
class FirestoreDataSource @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()

    private val snacksCollection = db.collection("snacks")
    private val categoriesCollection = db.collection("categories")

    // ── Snacks ──────────────────────────────────────
    suspend fun getAllSnacks(): List<Snack> {
        val snapshot = snacksCollection.orderBy("id").get().await()
        return snapshot.documents.mapNotNull { it.toObject(FirestoreSnack::class.java)?.toDomain() }
    }

    suspend fun addSnack(snack: Snack) {
        snacksCollection.document(snack.id.toString()).set(FirestoreSnack.fromDomain(snack)).await()
    }

    suspend fun updateSnack(snack: Snack) {
        snacksCollection.document(snack.id.toString()).set(FirestoreSnack.fromDomain(snack)).await()
    }

    suspend fun deleteSnack(snackId: Int) {
        snacksCollection.document(snackId.toString()).delete().await()
    }

    suspend fun getNextSnackId(): Int {
        val snapshot = snacksCollection.orderBy("id", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1).get().await()
        val maxId = snapshot.documents.firstOrNull()?.getLong("id")?.toInt() ?: 0
        return maxId + 1
    }
}
