package com.keciput.asrifa.data.repository

import com.keciput.asrifa.data.local.dao.OrderHistoryDao
import com.keciput.asrifa.data.local.entity.OrderHistoryEntity
import com.keciput.asrifa.domain.model.CartItem
import com.keciput.asrifa.domain.model.OrderHistory
import com.keciput.asrifa.domain.model.PackagingType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderHistoryRepositoryImpl @Inject constructor(
    private val orderHistoryDao: OrderHistoryDao,
    private val gson: Gson
) : OrderHistoryRepository {

    override fun getOrderHistoryByUser(userId: String): Flow<List<OrderHistory>> {
        return orderHistoryDao.getOrderHistoryByUser(userId).map { entities ->
            entities.map { it.asDomainModel(gson) }
        }
    }

    override suspend fun saveOrderHistory(order: OrderHistory) {
        orderHistoryDao.insertOrderHistory(order.asEntity(gson))
    }
}

private data class CartItemJson(
    val id: String,
    val snackId: Int,
    val snackName: String,
    val imageUrl: String,
    val selectedVariant: String?,
    val packagingType: String,
    val quantity: Int,
    val pricePerUnit: Double,
    val addedAt: Long
)

private fun OrderHistoryEntity.asDomainModel(gson: Gson): OrderHistory {
    val type = object : TypeToken<List<CartItemJson>>() {}.type
    val itemsJson: List<CartItemJson> = gson.fromJson(itemsJson, type)
    val items = itemsJson.map { json ->
        CartItem(
            id = json.id,
            snackId = json.snackId,
            snackName = json.snackName,
            imageUrl = json.imageUrl,
            selectedVariant = json.selectedVariant,
            packagingType = PackagingType.valueOf(json.packagingType),
            quantity = json.quantity,
            pricePerUnit = json.pricePerUnit,
            addedAt = json.addedAt
        )
    }
    return OrderHistory(
        id = id,
        userId = userId,
        orderDate = orderDate,
        items = items,
        totalPrice = totalPrice,
        note = note,
        itemCount = itemCount
    )
}

private fun OrderHistory.asEntity(gson: Gson): OrderHistoryEntity {
    val itemsJson = items.map { item ->
        CartItemJson(
            id = item.id,
            snackId = item.snackId,
            snackName = item.snackName,
            imageUrl = item.imageUrl,
            selectedVariant = item.selectedVariant,
            packagingType = item.packagingType.name,
            quantity = item.quantity,
            pricePerUnit = item.pricePerUnit,
            addedAt = item.addedAt
        )
    }
    return OrderHistoryEntity(
        id = id,
        userId = userId,
        orderDate = orderDate,
        itemsJson = gson.toJson(itemsJson),
        totalPrice = totalPrice,
        note = note,
        itemCount = itemCount
    )
}
