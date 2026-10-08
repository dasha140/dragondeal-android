package com.elvsf.dragondeal.storage

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*

/**
 * Unit tests for TokenStorage.
 * Uses mocked SharedPreferences to verify save/retrieval/clear operations.
 */
class TokenStorageTest {

    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var tokenStorage: TokenStorage

    // Simulated in-memory store to back the mock SharedPreferences
    private val data = mutableMapOf<String, Any>()

    @Before
    fun setUp() {
        mockContext = mock(Context::class.java)
        mockPrefs = mock(SharedPreferences::class.java)
        mockEditor = mock(SharedPreferences.Editor::class.java)

        // Context chain: getApplicationContext() returns the same mock
        `when`(mockContext.getApplicationContext()).thenReturn(mockContext)

        // getSharedPreferences returns our mock prefs
        `when`(mockContext.getSharedPreferences(eq("dragondeal_auth"), anyInt())).thenReturn(mockPrefs)

        // Editor chaining: putString and putLong return the editor itself
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.putLong(anyString(), anyLong())).thenReturn(mockEditor)
        `when`(mockEditor.clear()).thenReturn(mockEditor)

        // prefs.edit() returns our mock editor
        `when`(mockPrefs.edit()).thenReturn(mockEditor)

        // prefs.getString and getLong use our in-memory map
        `when`(mockPrefs.getString(anyString(), anyString())).thenAnswer { invocation ->
            val key = invocation.getArgument<String>(0)
            val default = invocation.getArgument<String>(1)
            data[key] as? String? ?: default
        }
        `when`(mockPrefs.getLong(anyString(), anyLong())).thenAnswer { invocation ->
            val key = invocation.getArgument<String>(0)
            val default = invocation.getArgument<Long>(1)
            (data[key] as? Long?) ?: default
        }

        // When editor.putString is called, store in our map
        `when`(mockEditor.putString(anyString(), anyString())).thenAnswer { invocation ->
            val key = invocation.getArgument<String>(0)
            val value = invocation.getArgument<String>(1)
            data[key] = value
            mockEditor
        }
        `when`(mockEditor.putLong(anyString(), anyLong())).thenAnswer { invocation ->
            val key = invocation.getArgument<String>(0)
            val value = invocation.getArgument<Long>(1)
            data[key] = value
            mockEditor
        }

        // When editor.clear is called, clear our map
        `when`(mockEditor.clear()).thenAnswer {
            data.clear()
            mockEditor
        }

        tokenStorage = TokenStorage(mockContext)
    }

    /* ---------- save and retrieve ---------- */

    @Test
    fun `saveAuthData stores all fields and they can be retrieved`() {
        tokenStorage.saveAuthData(
            accessToken = "token123",
            userId = 42L,
            username = "testuser",
            email = "test@example.com",
            role = "seller"
        )

        assertEquals("token123", tokenStorage.accessToken)
        assertEquals(42L, tokenStorage.userId)
        assertEquals("testuser", tokenStorage.username)
        assertEquals("test@example.com", tokenStorage.email)
        assertEquals("seller", tokenStorage.role)
    }

    @Test
    fun `getAccessToken returns empty string when no token saved`() {
        assertEquals("", tokenStorage.accessToken)
    }

    @Test
    fun `getUserId returns 0L when no user saved`() {
        assertEquals(0L, tokenStorage.userId)
    }

    @Test
    fun `getUsername returns empty string when no username saved`() {
        assertEquals("", tokenStorage.username)
    }

    @Test
    fun `getEmail returns empty string when no email saved`() {
        assertEquals("", tokenStorage.email)
    }

    @Test
    fun `getRole returns empty string when no role saved`() {
        assertEquals("", tokenStorage.role)
    }

    /* ---------- isLoggedIn ---------- */

    @Test
    fun `isLoggedIn returns false when no token`() {
        assertFalse(tokenStorage.isLoggedIn)
    }

    @Test
    fun `isLoggedIn returns true when token exists`() {
        tokenStorage.saveAuthData("some-token", 1L, "user", "a@b.com", "buyer")
        assertTrue(tokenStorage.isLoggedIn)
    }

    @Test
    fun `isLoggedIn returns false when token is empty string`() {
        tokenStorage.saveAuthData("", 1L, "user", "a@b.com", "buyer")
        assertFalse(tokenStorage.isLoggedIn)
    }

    @Test
    fun `isLoggedIn returns false when token is whitespace only`() {
        tokenStorage.saveAuthData("   ", 1L, "user", "a@b.com", "buyer")
        assertFalse(tokenStorage.isLoggedIn)
    }

    /* ---------- clear ---------- */

    @Test
    fun `clear removes all stored data`() {
        tokenStorage.saveAuthData("token", 1L, "user", "a@b.com", "seller")
        assertTrue(tokenStorage.isLoggedIn)

        tokenStorage.clear()

        assertEquals("", tokenStorage.accessToken)
        assertEquals(0L, tokenStorage.userId)
        assertEquals("", tokenStorage.username)
        assertFalse(tokenStorage.isLoggedIn)
    }

    @Test
    fun `clear calls editor clear and apply`() {
        tokenStorage.clear()
        verify(mockEditor).clear()
        verify(mockEditor).apply()
    }

    /* ---------- null handling ---------- */

    @Test
    fun `saveAuthData with null values stores empty strings`() {
        tokenStorage.saveAuthData(null, null, null, null, null)

        assertEquals("", tokenStorage.accessToken)
        assertEquals(0L, tokenStorage.userId)
        assertEquals("", tokenStorage.username)
        assertEquals("", tokenStorage.email)
        assertEquals("", tokenStorage.role)
    }

    /* ---------- overwrite ---------- */

    @Test
    fun `saveAuthData overwrites previous values`() {
        tokenStorage.saveAuthData("token1", 1L, "user1", "a@b.com", "buyer")
        tokenStorage.saveAuthData("token2", 2L, "user2", "c@d.com", "seller")

        assertEquals("token2", tokenStorage.accessToken)
        assertEquals(2L, tokenStorage.userId)
        assertEquals("user2", tokenStorage.username)
        assertEquals("c@d.com", tokenStorage.email)
        assertEquals("seller", tokenStorage.role)
    }
}
