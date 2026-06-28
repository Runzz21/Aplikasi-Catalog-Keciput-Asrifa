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

class GetFeaturedSnacksUseCaseTest {

    @RelaxedMockK
    private lateinit var repository: SnackRepository

    private lateinit var useCase: GetFeaturedSnacksUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetFeaturedSnacksUseCase(repository)
    }

    @Test
    fun `returns featured snacks when available`() = runTest {
        every { repository.getAllSnacks() } returns flowOf(
            listOf(snack(1, isFeatured = true), snack(2, isFeatured = true))
        )

        val result = useCase().first()

        assertEquals(2, result.size)
    }

    @Test
    fun `falls back to top-rated when no featured snacks`() = runTest {
        val snacks = (1..10).map { snack(it, rating = it.toDouble(), isFeatured = false) }
        every { repository.getAllSnacks() } returns flowOf(snacks)

        val result = useCase().first()

        assertEquals(6, result.size)
        assertEquals(10.0, result[0].rating, 0.0)
    }

    @Test
    fun `limits featured to 6 items`() = runTest {
        val featured = (1..10).map { snack(it, isFeatured = true) }
        every { repository.getAllSnacks() } returns flowOf(featured)

        val result = useCase().first()

        assertEquals(6, result.size)
    }

    private fun snack(id: Int, rating: Double = 5.0, isFeatured: Boolean = false) = Snack(
        id = id, name = "Snack $id", imageUrl = "", price = 1000.0,
        rating = rating, category = "Snack", isFeatured = isFeatured
    )
}
