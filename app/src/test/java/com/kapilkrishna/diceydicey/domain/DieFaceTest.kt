package com.kapilkrishna.diceydicey.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DieFaceTest {

    @Test
    fun `fromInt maps 1 through 6 to the matching face`() {
        (1..6).forEach { value ->
            assertEquals(value, DieFace.fromInt(value).pips)
        }
    }

    @Test
    fun `dotGrid is always a 3x3 grid`() {
        DieFace.entries.forEach { face ->
            val grid = face.dotGrid()
            assertEquals(3, grid.size)
            grid.forEach { row -> assertEquals(3, row.size) }
        }
    }

    @Test
    fun `dotGrid lights exactly as many dots as pips`() {
        DieFace.entries.forEach { face ->
            val dotCount = face.dotGrid().sumOf { row -> row.count { it } }
            assertEquals(face.pips, dotCount)
        }
    }

    @Test
    fun `one is a single centered dot`() {
        val grid = DieFace.ONE.dotGrid()
        assertTrue(grid[1][1])
    }

    @Test
    fun `four is four corners`() {
        val grid = DieFace.FOUR.dotGrid()
        assertTrue(grid[0][0])
        assertTrue(grid[0][2])
        assertTrue(grid[2][0])
        assertTrue(grid[2][2])
        assertFalse(grid[1][1])
    }

    @Test
    fun `six is two full side columns with an empty middle column`() {
        val grid = DieFace.SIX.dotGrid()
        for (row in 0..2) {
            assertTrue(grid[row][0])
            assertFalse(grid[row][1])
            assertTrue(grid[row][2])
        }
    }
}
