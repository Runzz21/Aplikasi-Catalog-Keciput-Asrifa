package com.keciput.asrifa.data.repository

import com.keciput.asrifa.data.local.entity.CartEntity
import com.keciput.asrifa.data.local.entity.CategoryEntity
import com.keciput.asrifa.data.local.entity.ReviewEntity
import com.keciput.asrifa.data.local.entity.SnackEntity
import com.keciput.asrifa.domain.model.CartItem
import com.keciput.asrifa.domain.model.Category
import com.keciput.asrifa.domain.model.Review
import com.keciput.asrifa.domain.model.Snack

fun SnackEntity.asDomainModel(): Snack {
    return Snack(
        id = id,
        name = name ?: "",
        imageUrl = imageUrl ?: "",
        price = price,
        originalPrice = originalPrice,
        rating = rating,
        category = category ?: "",
        categoryId = categoryId ?: "",
        isAvailable = isAvailable,
        description = description ?: "",
        reviewCount = reviewCount,
        weightDefault = weightDefault ?: "250g",
        variants = if (variants.isNullOrEmpty()) emptyList() else variants.split(","),
        hasBulkPackaging = hasBulkPackaging,
        isBestseller = isBestseller,
        isNew = isNew,
        isHot = isHot,
        isFeatured = isFeatured,
        isFlashSale = isFlashSale,
        soldCount = soldCount
    )
}

fun CategoryEntity.asDomainModel(): Category {
    return Category(
        id = id,
        name = name,
        iconUrl = iconUrl
    )
}

fun CartEntity.asDomainModel(): CartItem {
    return CartItem(
        id = id,
        snackId = snackId,
        snackName = snackName,
        imageUrl = imageUrl,
        selectedVariant = selectedVariant,
        packagingType = packagingType,
        quantity = quantity,
        pricePerUnit = pricePerUnit,
        addedAt = addedAt
    )
}

fun CartItem.asEntity(): CartEntity {
    return CartEntity(
        id = id,
        snackId = snackId,
        snackName = snackName,
        imageUrl = imageUrl,
        selectedVariant = selectedVariant,
        packagingType = packagingType,
        quantity = quantity,
        pricePerUnit = pricePerUnit,
        addedAt = addedAt
    )
}

fun ReviewEntity.asDomainModel(): Review {
    return Review(
        id = id,
        snackId = snackId,
        userName = userName,
        rating = rating,
        comment = comment,
        reviewDate = reviewDate
    )
}
