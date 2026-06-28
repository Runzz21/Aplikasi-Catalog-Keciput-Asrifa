package com.keciput.asrifa.domain.model

data class Snack(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val price: Double,
    val originalPrice: Double? = null,
    val rating: Double,
    val category: String,
    val categoryId: String = "",
    val isAvailable: Boolean = true,
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
)

data class Category(
    val id: String,
    val name: String,
    val iconUrl: String,
    val iconRes: Int? = null,
    val productCount: Int = 0
)

data class Banner(
    val id: Int,
    val title: String,
    val subtitle: String,
    val promoLabel: String,
    val imageUrl: Any = ""
)

data class CartItem(
    val id: String,
    val snackId: Int,
    val snackName: String,
    val imageUrl: String,
    val selectedVariant: String? = null,
    val packagingType: PackagingType = PackagingType.ECERAN,
    val quantity: Int,
    val pricePerUnit: Double,
    val addedAt: Long = System.currentTimeMillis()
) {
    val subtotal: Double
        get() = quantity * pricePerUnit
}

enum class PackagingType {
    ECERAN, LOS
}

data class Review(
    val id: String,
    val snackId: Int,
    val userName: String,
    val rating: Int,
    val comment: String,
    val reviewDate: Long
)

data class PackagingOption(
    val type: PackagingType,
    val weightLabel: String,
    val priceInfo: String
)

data class OrderHistory(
    val id: String,
    val userId: String,
    val orderDate: Long,
    val items: List<CartItem>,
    val totalPrice: Double,
    val note: String,
    val itemCount: Int
)

data class StoreInfo(
    val openTime: String = "08:00",          // format "HH:mm"
    val closeTime: String = "17:00",
    val operatingDays: List<Int> = listOf(1,2,3,4,5,6), // 0=Minggu .. 6=Sabtu
    val holidayDates: List<String> = emptyList(),        // format "yyyy-MM-dd"
    val holidayMessage: String = "",
    val isNotificationEnabled: Boolean = true            // toggle pengguna
)
