package com.keciput.asrifa.data.repository

import com.keciput.asrifa.data.local.dao.CartDao
import com.keciput.asrifa.domain.model.CartItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao
) : CartRepository {

    override fun getCartItems(): Flow<List<CartItem>> {
        return cartDao.getAllCartItems().map { entities ->
            entities.map { it.asDomainModel() }
        }
    }

    override fun getCartCount(): Flow<Int> {
        return cartDao.getCartCount().map { it ?: 0 }
    }

    override suspend fun addToCart(cartItem: CartItem) {
        val existing = cartDao.getCartItem(
            snackId = cartItem.snackId,
            variant = cartItem.selectedVariant,
            packagingType = cartItem.packagingType.name
        )
        
        if (existing != null) {
            cartDao.updateQuantity(existing.id, existing.quantity + cartItem.quantity)
        } else {
            cartDao.upsertCartItem(cartItem.asEntity())
        }
    }

    override suspend fun updateQuantity(cartItemId: String, quantity: Int) {
        if (quantity <= 0) {
            cartDao.deleteCartItemById(cartItemId)
        } else {
            cartDao.updateQuantity(cartItemId, quantity)
        }
    }

    override suspend fun removeFromCart(cartItemId: String) {
        cartDao.deleteCartItemById(cartItemId)
    }

    override suspend fun clearCart() {
        cartDao.clearCart()
    }
}
