package com.keciput.asrifa.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SnackToCartItemTest {

    private val snack = Snack(
        id = 42, name = "Keciput Original", imageUrl = "img.jpg",
        price = 15000.0, rating = 4.5, category = "Snack"
    )

    @Test
    fun `toCartItem maps fields correctly`() {
        val item = snack.toCartItem()

        assertEquals(42, item.snackId)
        assertEquals("Keciput Original", item.snackName)
        assertEquals("img.jpg", item.imageUrl)
        assertEquals(15000.0, item.pricePerUnit, 0.0)
        assertEquals(1, item.quantity)
        assertEquals(PackagingType.ECERAN, item.packagingType)
        assertNotNull(item.id)
    }

    @Test
    fun `toCartItem with custom params`() {
        val item = snack.toCartItem(
            quantity = 3,
            packagingType = PackagingType.LOS,
            selectedVariant = "Pedas"
        )

        assertEquals(3, item.quantity)
        assertEquals(PackagingType.LOS, item.packagingType)
        assertEquals("Pedas", item.selectedVariant)
    }

    @Test
    fun `subtotal is quantity times price`() {
        val item = snack.toCartItem(quantity = 5)

        assertEquals(75000.0, item.subtotal, 0.0)
    }
}
