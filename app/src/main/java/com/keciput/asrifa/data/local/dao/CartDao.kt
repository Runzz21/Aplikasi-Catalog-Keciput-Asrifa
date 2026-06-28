package com.keciput.asrifa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.keciput.asrifa.data.local.entity.CartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_entity ORDER BY addedAt DESC")
    fun getAllCartItems(): Flow<List<CartEntity>>

    @Query("SELECT * FROM cart_entity WHERE snackId = :snackId AND selectedVariant = :variant AND packagingType = :packagingType LIMIT 1")
    suspend fun getCartItem(snackId: Int, variant: String?, packagingType: String): CartEntity?

    @Upsert
    suspend fun upsertCartItem(cartItem: CartEntity)

    @Query("UPDATE cart_entity SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: String, quantity: Int)

    @Delete
    suspend fun deleteCartItem(cartItem: CartEntity)

    @Query("DELETE FROM cart_entity WHERE id = :id")
    suspend fun deleteCartItemById(id: String)

    @Query("DELETE FROM cart_entity")
    suspend fun clearCart()

    @Query("SELECT SUM(quantity) FROM cart_entity")
    fun getCartCount(): Flow<Int?>
}
