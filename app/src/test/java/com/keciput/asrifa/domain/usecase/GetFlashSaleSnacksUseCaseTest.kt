package com.keciput.asrifa.domain.usecase

import com.keciput.asrifa.data.repository.SnackRepository
import com.keciput.asrifa.domain.model.Snack
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetFlashSaleSnacksUseCaseTest {

    @RelaxedMockK
    private lateinit var repository: SnackRepository

    private lateinit var useCase: GetFlashSaleSnacksUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetFlashSaleSnacksUseCase(repository)
    }

    @Test
    fun `returns flash sale snacks when available`() = runTest {
        every { repository.getAllSnacks() } returns flowOf(
            listOf(snack(1, isFlashSale = true), snack(2, isFlashSale = true))
        )

        val result = useCase().first()

        assertEquals(2, result.size)
    }

    @Test
    fun `falls back to random snacks when no flash sale`() = runTest {
        val snacks = (1..10).map { snack(it) }
        every { repository.getAllSnacks() } returns flowOf(snacks)

        val result = useCase().first()

        assertEquals(6, result.size)
        assertTrue(result.all { it.isFlashSale })
    }

    private fun snack(id: Int, isFlashSale: Boolean = false) = Snack(
        id = id, name = "Snack $id", imageUrl = "", price = 1000.0,
        rating = 5.0, category = "Snack", isFlashSale = isFlashSale
    )
}
