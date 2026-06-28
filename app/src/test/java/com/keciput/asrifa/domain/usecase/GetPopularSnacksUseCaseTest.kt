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
import org.junit.Before
import org.junit.Test

class GetPopularSnacksUseCaseTest {

    @RelaxedMockK
    private lateinit var repository: SnackRepository

    private lateinit var useCase: GetPopularSnacksUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetPopularSnacksUseCase(repository)
    }

    @Test
    fun `returns bestseller when available`() = runTest {
        every { repository.getAllSnacks() } returns flowOf(
            listOf(snack(1, isBestseller = true), snack(2, isBestseller = true))
        )

        val result = useCase().first()

        assertEquals(2, result.size)
    }

    @Test
    fun `falls back to top-selling when no bestseller`() = runTest {
        val snacks = (1..10).map { snack(it, isBestseller = false, soldCount = it * 10) }
        every { repository.getAllSnacks() } returns flowOf(snacks)

        val result = useCase().first()

        assertEquals(6, result.size)
        assertEquals(100, result[0].soldCount)
    }

    @Test
    fun `sorts bestseller by soldCount descending`() = runTest {
        every { repository.getAllSnacks() } returns flowOf(
            listOf(
                snack(1, isBestseller = true, soldCount = 30),
                snack(2, isBestseller = true, soldCount = 100),
                snack(3, isBestseller = true, soldCount = 50),
            )
        )

        val result = useCase().first()

        assertEquals(2, result[0].id)
        assertEquals(3, result[1].id)
        assertEquals(1, result[2].id)
    }

    private fun snack(id: Int, isBestseller: Boolean = false, soldCount: Int = 0) = Snack(
        id = id, name = "Snack $id", imageUrl = "", price = 1000.0,
        rating = 5.0, category = "Snack", isBestseller = isBestseller,
        soldCount = soldCount
    )
}
