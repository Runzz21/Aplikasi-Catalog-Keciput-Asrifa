package com.keciput.asrifa.data.repository

import com.keciput.asrifa.domain.model.CartItem
import com.keciput.asrifa.domain.model.Category
import com.keciput.asrifa.domain.model.Review
import com.keciput.asrifa.domain.model.Snack
import com.keciput.asrifa.domain.model.OrderHistory
import com.keciput.asrifa.domain.model.StoreInfo
import kotlinx.coroutines.flow.Flow

interface SnackRepository {
    fun getAllSnacks(): Flow<List<Snack>>
    fun getTopRatedSnacks(): Flow<List<Snack>>
    fun getSnacksByFilter(category: String?): Flow<List<Snack>>
    fun searchSnacks(query: String): Flow<List<Snack>>
    suspend fun getSnackById(id: Int): Snack?
}

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
}

interface CartRepository {
    fun getCartItems(): Flow<List<CartItem>>
    fun getCartCount(): Flow<Int>
    suspend fun addToCart(cartItem: CartItem)
    suspend fun updateQuantity(cartItemId: String, quantity: Int)
    suspend fun removeFromCart(cartItemId: String)
    suspend fun clearCart()
}

interface ReviewRepository {
    fun getReviewsForSnack(snackId: Int): Flow<List<Review>>
    suspend fun addReviews(reviews: List<Review>)
}

interface OrderHistoryRepository {
    fun getOrderHistoryByUser(userId: String): Flow<List<OrderHistory>>
    suspend fun saveOrderHistory(order: OrderHistory)
}

interface StoreInfoRepository {
    fun getStoreInfo(): Flow<StoreInfo?>
    suspend fun saveStoreInfo(storeInfo: StoreInfo)
}
