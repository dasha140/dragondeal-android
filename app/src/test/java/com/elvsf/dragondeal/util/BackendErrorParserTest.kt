package com.elvsf.dragondeal.util

import org.junit.Test
import org.junit.Assert.*
import java.net.SocketTimeoutException
import java.io.IOException

/**
 * Unit tests for BackendErrorParser.
 * Verifies that network and HTTP errors are correctly converted to Russian user messages.
 */
class BackendErrorParserTest {

    @Test
    fun `parse Throwable SocketTimeoutException returns connection message`() {
        val t = SocketTimeoutException("Read timed out")
        val message = BackendErrorParser.parse(t)
        assertNotNull(message)
        assertTrue(message.contains("соедин", ignoreCase = true))
    }

    @Test
    fun `parse Throwable IOException returns connection message`() {
        val t = IOException("Unable to resolve host")
        val message = BackendErrorParser.parse(t)
        assertNotNull(message)
        assertTrue(message.contains("соедин", ignoreCase = true))
    }

    @Test
    fun `parse null Throwable returns generic message`() {
        val message = BackendErrorParser.parse(null)
        assertNotNull(message)
        assertTrue(message.isNotEmpty())
    }

    @Test
    fun `parse generic Throwable returns generic message`() {
        val t = RuntimeException("Something went wrong")
        val message = BackendErrorParser.parse(t)
        assertNotNull(message)
        assertTrue(message.isNotEmpty())
    }
}
