package com.elvsf.dragondeal.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.elvsf.dragondeal.dto.OrderResponse
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for OrdersViewModel.
 * Verifies that order data is stored and retrieved correctly.
 */
class OrdersViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val viewModel = OrdersViewModel()

    @Test
    fun `initial loading state is false`() {
        val loading = viewModel.loading.value
        assertFalse(loading == true)
    }

    @Test
    fun `setBuyerOrders stores and retrieves orders`() {
        val orders = listOf(
            OrderResponse().apply { id = 1L },
            OrderResponse().apply { id = 2L }
        )

        viewModel.setBuyerOrders(orders)

        assertEquals(orders, viewModel.buyerOrders.value)
        assertEquals(2, viewModel.buyerOrders.value?.size)
    }

    @Test
    fun `setSellerOrders stores and retrieves orders`() {
        val orders = listOf(
            OrderResponse().apply { id = 3L }
        )

        viewModel.setSellerOrders(orders)

        assertEquals(orders, viewModel.sellerOrders.value)
    }

    @Test
    fun `setLoading updates loading state`() {
        viewModel.setLoading(true)
        assertTrue(viewModel.loading.value == true)

        viewModel.setLoading(false)
        assertFalse(viewModel.loading.value == true)
    }

    @Test
    fun `buyerOrders and sellerOrders are independent`() {
        val buyerOrders = listOf(OrderResponse().apply { id = 1L })
        val sellerOrders = listOf(OrderResponse().apply { id = 2L })

        viewModel.setBuyerOrders(buyerOrders)
        viewModel.setSellerOrders(sellerOrders)

        assertEquals(1L, viewModel.buyerOrders.value?.firstOrNull()?.id)
        assertEquals(2L, viewModel.sellerOrders.value?.firstOrNull()?.id)
    }

    @Test
    fun `setting null orders clears the list`() {
        viewModel.setBuyerOrders(listOf(OrderResponse().apply { id = 1L }))
        assertNotNull(viewModel.buyerOrders.value)

        viewModel.setBuyerOrders(null)
        assertNull(viewModel.buyerOrders.value)
    }
}
