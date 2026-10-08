package com.elvsf.dragondeal.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for MenuItemModel.
 * Verifies that the data class correctly stores and returns title and icon resource ID.
 */
class MenuItemModelTest {

    @Test
    fun `constructor stores title and iconResId`() {
        val item = MenuItemModel("Profile", 2131234567)

        assertEquals("Profile", item.title)
        assertEquals(2131234567, item.iconResId)
    }

    @Test
    fun `getTitle returns the exact title passed to constructor`() {
        val title = "Settings"
        val item = MenuItemModel(title, 100)

        assertEquals(title, item.getTitle())
    }

    @Test
    fun `getIconResId returns the exact icon resource ID passed to constructor`() {
        val iconResId = 999999
        val item = MenuItemModel("Test", iconResId)

        assertEquals(iconResId, item.getIconResId())
    }

    @Test
    fun `empty title is allowed`() {
        val item = MenuItemModel("", 0)

        assertEquals("", item.title)
        assertEquals(0, item.iconResId)
    }

    @Test
    fun `null title is stored as-is`() {
        val item = MenuItemModel(null, 123)

        assertNull(item.title)
        assertEquals(123, item.iconResId)
    }

    @Test
    fun `multiple instances are independent`() {
        val item1 = MenuItemModel("Home", 1)
        val item2 = MenuItemModel("About", 2)

        assertEquals("Home", item1.title)
        assertEquals("About", item2.title)
        assertEquals(1, item1.iconResId)
        assertEquals(2, item2.iconResId)
    }

    @Test
    fun `title with special characters is preserved`() {
        val item = MenuItemModel("Профиль / Profile", 2131000000)

        assertEquals("Профиль / Profile", item.title)
    }

    @Test
    fun `icon resource ID can be large values`() {
        val largeResId = Int.MAX_VALUE
        val item = MenuItemModel("Large Icon", largeResId)

        assertEquals(largeResId, item.iconResId)
    }
}
