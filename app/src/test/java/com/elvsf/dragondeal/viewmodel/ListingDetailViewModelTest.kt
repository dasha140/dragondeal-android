package com.elvsf.dragondeal.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.elvsf.dragondeal.api.ListingApi
import com.elvsf.dragondeal.dto.ListingResponse
import com.elvsf.dragondeal.util.ApiCallback
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import retrofit2.Call
import retrofit2.Response

/**
 * Unit tests for ListingDetailViewModel.
 * Verifies listing loading, seller listings, and UI state management.
 */
class ListingDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val viewModel = ListingDetailViewModel()
    private lateinit var mockApi: ListingApi
    private lateinit var mockCall: Call<ListingResponse>
    private lateinit var mockListCall: Call<List<ListingResponse>>

    @Before
    fun setUp() {
        mockApi = mock(ListingApi::class.java)
        mockCall = mock(Call::class.java) as Call<ListingResponse>
        mockListCall = mock(Call::class.java) as Call<List<ListingResponse>>
    }

    private fun mockListing(
        id: Long,
        title: String = "",
        sellerId: Long = 0L,
        categoryName: String = ""
    ): ListingResponse {
        return mock(ListingResponse::class.java).apply {
            `when`(this.id).thenReturn(id)
            `when`(this.title).thenReturn(title)
            `when`(this.sellerId).thenReturn(sellerId)
            `when`(this.categoryName).thenReturn(categoryName)
        }
    }

    /* ---------- initial state ---------- */

    @Test
    fun `initial loading state is false`() {
        assertFalse(viewModel.loading.value == true)
    }

    @Test
    fun `initial listing is null`() {
        assertNull(viewModel.listing.value)
    }

    @Test
    fun `initial sellerListings is null`() {
        assertNull(viewModel.sellerListings.value)
    }

    @Test
    fun `initial error is null`() {
        assertNull(viewModel.error.value)
    }

    /* ---------- loadListing success ---------- */

    @Test
    fun `loadListing onSuccess stores listing and clears loading`() {
        `when`(mockApi.findListingById(10L)).thenReturn(mockCall)

        viewModel.loadListing(mockApi, 10L)

        assertEquals(true, viewModel.loading.value)

        // Simulate API success callback
        val listing = mockListing(10L, title = "Test Listing")
        verify(mockCall).enqueue(any(ApiCallback::class.java))
        // Capture the callback and trigger onSuccess
        val callbackCaptor = argumentCaptor<ApiCallback<ListingResponse>>()
        verify(mockCall).enqueue(callbackCaptor.capture())
        callbackCaptor.firstValue.onResponse(mockCall, Response.success(listing))

        assertEquals(listing, viewModel.listing.value)
        assertEquals(false, viewModel.loading.value)
    }

    /* ---------- loadListing error ---------- */

    @Test
    fun `loadListing onError sets error message and clears loading`() {
        `when`(mockApi.findListingById(10L)).thenReturn(mockCall)

        viewModel.loadListing(mockApi, 10L)
        assertEquals(true, viewModel.loading.value)

        val callbackCaptor = argumentCaptor<ApiCallback<ListingResponse>>()
        verify(mockCall).enqueue(callbackCaptor.capture())

        // Trigger the showError path (which ApiCallback calls for HTTP errors)
        callbackCaptor.firstValue.run {
            // Simulate what ApiCallback does on error: call showError
            // We can access showError since it's protected — use reflection or test through the callback
            // Actually, ApiCallback's showError is protected, so we can't call it directly from here.
            // But we can call onFailure which triggers showError
            this.onFailure(mockCall, java.net.SocketTimeoutException("timeout"))
        }

        assertNotNull(viewModel.error.value)
        assertEquals(false, viewModel.loading.value)
    }

    /* ---------- loadSellerListings ---------- */

    @Test
    fun `loadSellerListings with null sellerId sets empty list`() {
        viewModel.loadSellerListings(mockApi, null, 10L)
        assertNotNull(viewModel.sellerListings.value)
        assertTrue(viewModel.sellerListings.value?.isEmpty() == true)
    }

    @Test
    fun `loadSellerListings onSuccess filters out current listing`() {
        `when`(mockApi.findListingsBySellerId(5L)).thenReturn(mockListCall)

        viewModel.loadSellerListings(mockApi, 5L, 10L)

        val otherListings = listOf(
            mockListing(10L, title = "Current"),   // should be excluded
            mockListing(11L, title = "Other 1"),
            mockListing(12L, title = "Other 2")
        )

        val callbackCaptor = argumentCaptor<ApiCallback<List<ListingResponse>>>()
        verify(mockListCall).enqueue(callbackCaptor.capture())
        callbackCaptor.firstValue.onResponse(mockListCall, Response.success(otherListings))

        val result = viewModel.sellerListings.value
        assertNotNull(result)
        assertEquals(2, result?.size)
        assertFalse(result?.any { it.id == 10L } == true)
    }

    @Test
    fun `loadSellerListings onSuccess handles null body`() {
        `when`(mockApi.findListingsBySellerId(5L)).thenReturn(mockListCall)

        viewModel.loadSellerListings(mockApi, 5L, 10L)

        val callbackCaptor = argumentCaptor<ApiCallback<List<ListingResponse>>>()
        verify(mockListCall).enqueue(callbackCaptor.capture())
        callbackCaptor.firstValue.onResponse(mockListCall, Response.success(null))

        val result = viewModel.sellerListings.value
        assertNotNull(result)
        assertTrue(result?.isEmpty() == true)
    }

    @Test
    fun `loadSellerListings onError sets empty list`() {
        `when`(mockApi.findListingsBySellerId(5L)).thenReturn(mockListCall)

        viewModel.loadSellerListings(mockApi, 5L, 10L)

        val callbackCaptor = argumentCaptor<ApiCallback<List<ListingResponse>>>()
        verify(mockListCall).enqueue(callbackCaptor.capture())
        callbackCaptor.firstValue.onFailure(mockListCall, RuntimeException("error"))

        val result = viewModel.sellerListings.value
        assertNotNull(result)
        assertTrue(result?.isEmpty() == true)
    }

    /* ---------- deactivateListing ---------- */

    @Test
    fun `deactivateListing onSuccess reloads the listing`() {
        val voidCall = mock(Call::class.java) as Call<Void>
        `when`(mockApi.deactivateListing(10L)).thenReturn(voidCall)
        `when`(mockApi.findListingById(10L)).thenReturn(mockCall)

        viewModel.deactivateListing(mockApi, 10L)
        assertEquals(true, viewModel.loading.value)

        val callbackCaptor = argumentCaptor<ApiCallback<Void>>()
        verify(voidCall).enqueue(callbackCaptor.capture())
        callbackCaptor.firstValue.onResponse(voidCall, Response.success(null))

        assertEquals(false, viewModel.loading.value)
        // loadListing was called, so another call should have been enqueued
        verify(mockApi).findListingById(10L)
    }

    @Test
    fun `deactivateListing onError sets error and clears loading`() {
        val voidCall = mock(Call::class.java) as Call<Void>
        `when`(mockApi.deactivateListing(10L)).thenReturn(voidCall)

        viewModel.deactivateListing(mockApi, 10L)

        val callbackCaptor = argumentCaptor<ApiCallback<Void>>()
        verify(voidCall).enqueue(callbackCaptor.capture())
        callbackCaptor.firstValue.onFailure(voidCall, RuntimeException("server error"))

        assertNotNull(viewModel.error.value)
        assertEquals(false, viewModel.loading.value)
    }
}
