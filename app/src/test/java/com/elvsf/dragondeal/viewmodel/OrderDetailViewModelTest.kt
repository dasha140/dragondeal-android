package com.elvsf.dragondeal.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.elvsf.dragondeal.api.OrderRepository
import com.elvsf.dragondeal.api.ReviewApi
import com.elvsf.dragondeal.dto.CreateReviewRequest
import com.elvsf.dragondeal.dto.OrderResponse
import com.elvsf.dragondeal.dto.ReviewResponse
import com.elvsf.dragondeal.util.ApiCallback
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import retrofit2.Call
import retrofit2.Response
import java.math.BigDecimal

/**
 * Unit tests for OrderDetailViewModel.
 * Verifies order loading, seller done marking, and review creation.
 */
class OrderDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val viewModel = OrderDetailViewModel()
    private lateinit var mockRepo: OrderRepository
    private lateinit var mockReviewApi: ReviewApi
    private lateinit var mockOrderCall: Call<OrderResponse>
    private lateinit var mockVoidCall: Call<Void>
    private lateinit var mockReviewCall: Call<ReviewResponse>

    @Before
    fun setUp() {
        mockRepo = mock(OrderRepository::class.java)
        mockReviewApi = mock(ReviewApi::class.java)
        mockOrderCall = mock(Call::class.java) as Call<OrderResponse>
        mockVoidCall = mock(Call::class.java) as Call<Void>
        mockReviewCall = mock(Call::class.java) as Call<ReviewResponse>
    }

    /* ---------- initial state ---------- */

    @Test
    fun `initial loading state is false`() {
        assertFalse(viewModel.loading.value == true)
    }

    @Test
    fun `initial order is null`() {
        assertNull(viewModel.order.value)
    }

    @Test
    fun `initial error is null`() {
        assertNull(viewModel.error.value)
    }

    /* ---------- loadOrder success ---------- */

    @Test
    fun `loadOrder onSuccess stores order and clears loading`() {
        `when`(mockRepo.findOrderById(42L)).thenReturn(mockOrderCall)

        viewModel.loadOrder(mockRepo, 42L)
        assertEquals(true, viewModel.loading.value)

        val callbackCaptor = argumentCaptor<ApiCallback<OrderResponse>>()
        verify(mockOrderCall).enqueue(callbackCaptor.capture())

        val orderData = OrderResponse()
        callbackCaptor.firstValue.onResponse(mockOrderCall, Response.success(orderData))

        assertEquals(orderData, viewModel.order.value)
        assertEquals(false, viewModel.loading.value)
    }

    /* ---------- loadOrder error ---------- */

    @Test
    fun `loadOrder onError sets error message and clears loading`() {
        `when`(mockRepo.findOrderById(42L)).thenReturn(mockOrderCall)

        viewModel.loadOrder(mockRepo, 42L)

        val callbackCaptor = argumentCaptor<ApiCallback<OrderResponse>>()
        verify(mockOrderCall).enqueue(callbackCaptor.capture())
        callbackCaptor.firstValue.onFailure(mockOrderCall, java.io.IOException("network error"))

        assertNotNull(viewModel.error.value)
        assertEquals(false, viewModel.loading.value)
    }

    /* ---------- markSellerDone success ---------- */

    @Test
    fun `markSellerDone onSuccess reloads order`() {
        `when`(mockRepo.markSellerDone(42L)).thenReturn(mockVoidCall)
        `when`(mockRepo.findOrderById(42L)).thenReturn(mockOrderCall)

        viewModel.markSellerDone(mockRepo, 42L)
        assertEquals(true, viewModel.loading.value)

        val callbackCaptor = argumentCaptor<ApiCallback<Void>>()
        verify(mockVoidCall).enqueue(callbackCaptor.capture())
        callbackCaptor.firstValue.onResponse(mockVoidCall, Response.success(null))

        assertEquals(false, viewModel.loading.value)
        verify(mockRepo).findOrderById(42L)
    }

    /* ---------- markSellerDone error ---------- */

    @Test
    fun `markSellerDone onError sets error and clears loading`() {
        `when`(mockRepo.markSellerDone(42L)).thenReturn(mockVoidCall)

        viewModel.markSellerDone(mockRepo, 42L)

        val callbackCaptor = argumentCaptor<ApiCallback<Void>>()
        verify(mockVoidCall).enqueue(callbackCaptor.capture())
        callbackCaptor.firstValue.onFailure(mockVoidCall, RuntimeException("forbidden"))

        assertNotNull(viewModel.error.value)
        assertEquals(false, viewModel.loading.value)
    }

    /* ---------- createReview ---------- */

    @Test
    fun `createReview onSuccess clears loading`() {
        val request = CreateReviewRequest().apply {
            orderId = 42L
            rating = 5
            comment = "Great seller!"
        }
        `when`(mockReviewApi.createReview(request)).thenReturn(mockReviewCall)

        viewModel.createReview(mockReviewApi, request)
        assertEquals(true, viewModel.loading.value)

        val callbackCaptor = argumentCaptor<ApiCallback<ReviewResponse>>()
        verify(mockReviewCall).enqueue(callbackCaptor.capture())
        callbackCaptor.firstValue.onResponse(mockReviewCall, Response.success(null))

        assertEquals(false, viewModel.loading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `createReview onError sets error and clears loading`() {
        val request = CreateReviewRequest().apply {
            orderId = 42L
            rating = 4
            comment = "Good"
        }
        `when`(mockReviewApi.createReview(request)).thenReturn(mockReviewCall)

        viewModel.createReview(mockReviewApi, request)

        val callbackCaptor = argumentCaptor<ApiCallback<ReviewResponse>>()
        verify(mockReviewCall).enqueue(callbackCaptor.capture())
        callbackCaptor.firstValue.onFailure(mockReviewCall, RuntimeException("already reviewed"))

        assertNotNull(viewModel.error.value)
        assertEquals(false, viewModel.loading.value)
    }

    /* ---------- Order data access ---------- */

    @Test
    fun `getOrder returns LiveData with order data`() {
        `when`(mockRepo.findOrderById(1L)).thenReturn(mockOrderCall)
        viewModel.loadOrder(mockRepo, 1L)

        val callbackCaptor = argumentCaptor<ApiCallback<OrderResponse>>()
        verify(mockOrderCall).enqueue(callbackCaptor.capture())

        val order = OrderResponse()
        callbackCaptor.firstValue.onResponse(mockOrderCall, Response.success(order))

        val liveData = viewModel.order
        assertEquals(order, liveData.value)
    }

    @Test
    fun `getLoading returns correct state transitions`() {
        `when`(mockRepo.findOrderById(1L)).thenReturn(mockOrderCall)

        assertEquals(false, viewModel.loading.value)

        viewModel.loadOrder(mockRepo, 1L)
        assertEquals(true, viewModel.loading.value)

        val callbackCaptor = argumentCaptor<ApiCallback<OrderResponse>>()
        verify(mockOrderCall).enqueue(callbackCaptor.capture())
        callbackCaptor.firstValue.onResponse(mockOrderCall, Response.success(OrderResponse()))
        assertEquals(false, viewModel.loading.value)
    }

    @Test
    fun `getError returns null after successful load`() {
        `when`(mockRepo.findOrderById(1L)).thenReturn(mockOrderCall)

        viewModel.loadOrder(mockRepo, 1L)

        val callbackCaptor = argumentCaptor<ApiCallback<OrderResponse>>()
        verify(mockOrderCall).enqueue(callbackCaptor.capture())
        callbackCaptor.firstValue.onResponse(mockOrderCall, Response.success(OrderResponse()))

        assertNull(viewModel.error.value)
    }
}
