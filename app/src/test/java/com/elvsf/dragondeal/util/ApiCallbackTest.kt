package com.elvsf.dragondeal.util

import android.content.Context
import android.content.Intent
import okhttp3.ResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Unit tests for ApiCallback.
 * Verifies that API responses (success, auth errors, network failures)
 * are handled correctly with proper delegation to onSuccess / showError / auth redirect.
 */
class ApiCallbackTest {

    private lateinit var mockContext: Context
    private lateinit var mockCall: Call<String>

    @Before
    fun setUp() {
        mockContext = mock(Context::class.java)
        `when`(mockContext.getApplicationContext()).thenReturn(mockContext)
        mockCall = mock(Call::class.java) as Call<String>
    }

    /* ---------- onSuccess path ---------- */

    @Test
    fun `onResponse with 200 and body calls onSuccess`() {
        var onSuccessCalled = false
        val body = "success"
        val response = Response.success(body)

        val callback = object : ApiCallback<String>(mockContext) {
            override fun onSuccess(response: Response<String>) {
                onSuccessCalled = true
                assertEquals(body, response.body())
            }
        }

        callback.onResponse(mockCall, response)
        assertTrue("onSuccess should be called for 200 response", onSuccessCalled)
        verify(mockContext, never()).startActivity(any())
    }

    @Test
    fun `onResponse with 201 and body calls onSuccess`() {
        var onSuccessCalled = false
        val callback = object : ApiCallback<String>(mockContext) {
            override fun onSuccess(response: Response<String>) {
                onSuccessCalled = true
            }
        }

        val response = Response.success(201, ResponseBody.create(null, "created"))
        callback.onResponse(mockCall, response)
        assertTrue(onSuccessCalled)
    }

    @Test
    fun `onResponse with 200 but null body does NOT call onSuccess`() {
        var onSuccessCalled = false
        val callback = object : ApiCallback<String>(mockContext) {
            override fun onSuccess(response: Response<String>) {
                onSuccessCalled = true
            }
        }

        // Response.success(null) for a non-void type gives a null body
        val response: Response<String> = Response.success(null)
        callback.onResponse(mockCall, response)
        assertFalse("onSuccess should NOT be called when body is null", onSuccessCalled)
    }

    /* ---------- Auth error via HTTP status code ---------- */

    @Test
    fun `onResponse with 401 triggers auth redirect`() {
        val callback = object : ApiCallback<String>(mockContext) {
            override fun onSuccess(response: Response<String>) {
                fail("onSuccess should not be called for 401")
            }
        }

        val errorBody = ResponseBody.create(null, """{"error":"unauthorized"}""")
        val response: Response<String> = Response.error(401, errorBody)
        callback.onResponse(mockCall, response)

        verify(mockContext).startActivity(argThat<Intent> {
            component?.className?.contains("SigninActivity") == true
        })
    }

    @Test
    fun `onResponse with 403 triggers auth redirect`() {
        val callback = object : ApiCallback<String>(mockContext) {
            override fun onSuccess(response: Response<String>) {
                fail("onSuccess should not be called for 403")
            }
        }

        val response: Response<String> = Response.error(403, ResponseBody.create(null, "forbidden"))
        callback.onResponse(mockCall, response)

        verify(mockContext).startActivity(argThat<Intent> {
            component?.className?.contains("SigninActivity") == true
        })
    }

    @Test
    fun `onResponse with 400 and auth message in body triggers auth redirect`() {
        val callback = object : ApiCallback<String>(mockContext) {
            override fun onSuccess(response: Response<String>) {
                fail("onSuccess should not be called")
            }
        }

        val errorBody = ResponseBody.create(null, """{"message":"user not found"}""")
        val response: Response<String> = Response.error(400, errorBody)
        callback.onResponse(mockCall, response)

        verify(mockContext).startActivity(any(Intent::class.java))
    }

    /* ---------- Non-auth HTTP errors ---------- */

    @Test
    fun `onResponse with 500 shows error toast`() {
        var showErrorCalled = false
        var capturedMessage: String? = null
        val callback = object : ApiCallback<String>(mockContext) {
            override fun onSuccess(response: Response<String>) {
                fail("onSuccess should not be called for 500")
            }

            override fun showError(message: String) {
                showErrorCalled = true
                capturedMessage = message
            }
        }

        val response: Response<String> = Response.error(500, ResponseBody.create(null, "Internal Server Error"))
        callback.onResponse(mockCall, response)

        assertTrue(showErrorCalled)
        assertNotNull(capturedMessage)
        verify(mockContext, never()).startActivity(any())
    }

    @Test
    fun `onResponse with 404 shows error toast`() {
        var showErrorCalled = false
        val callback = object : ApiCallback<String>(mockContext) {
            override fun onSuccess(response: Response<String>) {
                fail("onSuccess should not be called for 404")
            }

            override fun showError(message: String) {
                showErrorCalled = true
            }
        }

        val response: Response<String> = Response.error(404, ResponseBody.create(null, "Not Found"))
        callback.onResponse(mockCall, response)

        assertTrue(showErrorCalled)
        verify(mockContext, never()).startActivity(any())
    }

    /* ---------- Network failure (onFailure) ---------- */

    @Test
    fun `onFailure with SocketTimeoutException shows error`() {
        var showErrorCalled = false
        var capturedMessage: String? = null
        val callback = object : ApiCallback<String>(mockContext) {
            override fun onSuccess(response: Response<String>) {
                fail("onSuccess should not be called")
            }

            override fun showError(message: String) {
                showErrorCalled = true
                capturedMessage = message
            }
        }

        callback.onFailure(mockCall, SocketTimeoutException("Read timed out"))

        assertTrue(showErrorCalled)
        assertNotNull(capturedMessage)
        verify(mockContext, never()).startActivity(any())
    }

    @Test
    fun `onFailure with generic IOException shows error`() {
        var showErrorCalled = false
        val callback = object : ApiCallback<String>(mockContext) {
            override fun onSuccess(response: Response<String>) = Unit
            override fun showError(message: String) {
                showErrorCalled = true
            }
        }

        callback.onFailure(mockCall, IOException("Unable to resolve host"))

        assertTrue(showErrorCalled)
        verify(mockContext, never()).startActivity(any())
    }

    @Test
    fun `onFailure with auth message triggers auth redirect`() {
        val callback = object : ApiCallback<String>(mockContext) {
            override fun onSuccess(response: Response<String>) {
                fail("onSuccess should not be called")
            }
        }

        callback.onFailure(mockCall, RuntimeException("JWT token expired"))

        verify(mockContext).startActivity(any(Intent::class.java))
    }

    /* ---------- isAuthMessage ---------- */

    @Test
    fun `isAuthMessage returns true for user not found`() {
        // isAuthMessage is private, but we can verify behavior through isAuthError
        val errorBody = ResponseBody.create(null, "user not found")
        val response: Response<String> = Response.error(400, errorBody)

        val callback = object : ApiCallback<String>(mockContext) {
            override fun onSuccess(response: Response<String>) = Unit
        }
        callback.onResponse(mockCall, response)

        verify(mockContext).startActivity(any(Intent::class.java))
    }

    @Test
    fun `isAuthMessage returns true for invalid or expired token`() {
        val errorBody = ResponseBody.create(null, "invalid or expired token")
        val response: Response<String> = Response.error(400, errorBody)

        val callback = object : ApiCallback<String>(mockContext) {
            override fun onSuccess(response: Response<String>) = Unit
        }
        callback.onResponse(mockCall, response)

        verify(mockContext).startActivity(any(Intent::class.java))
    }

    @Test
    fun `isAuthMessage returns true for JWT keyword`() {
        val errorBody = ResponseBody.create(null, "JWT verification failed")
        val response: Response<String> = Response.error(400, errorBody)

        val callback = object : ApiCallback<String>(mockContext) {
            override fun onSuccess(response: Response<String>) = Unit
        }
        callback.onResponse(mockCall, response)

        verify(mockContext).startActivity(any(Intent::class.java))
    }

    @Test
    fun `isAuthMessage returns true for authorization token is required`() {
        val errorBody = ResponseBody.create(null, "authorization token is required")
        val response: Response<String> = Response.error(400, errorBody)

        val callback = object : ApiCallback<String>(mockContext) {
            override fun onSuccess(response: Response<String>) = Unit
        }
        callback.onResponse(mockCall, response)

        verify(mockContext).startActivity(any(Intent::class.java))
    }

    @Test
    fun `isAuthMessage returns true for unauthorized`() {
        val errorBody = ResponseBody.create(null, "unauthorized access")
        val response: Response<String> = Response.error(400, errorBody)

        val callback = object : ApiCallback<String>(mockContext) {
            override fun onSuccess(response: Response<String>) = Unit
        }
        callback.onResponse(mockCall, response)

        verify(mockContext).startActivity(any(Intent::class.java))
    }

    @Test
    fun `auth error does NOT trigger redirect for non-auth message`() {
        var showErrorCalled = false
        val callback = object : ApiCallback<String>(mockContext) {
            override fun onSuccess(response: Response<String>) = Unit
            override fun showError(message: String) {
                showErrorCalled = true
            }
        }

        val errorBody = ResponseBody.create(null, "invalid request parameters")
        val response: Response<String> = Response.error(400, errorBody)
        callback.onResponse(mockCall, response)

        assertTrue(showErrorCalled)
        verify(mockContext, never()).startActivity(any())
    }
}
