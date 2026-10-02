package com.kapilkrishna.diceydicey.domain

import kotlin.random.Random
import org.junit.Assert.assertTrue
import org.junit.Test

class DiceRollerTest {

    @Test
    fun `roll returns a value between 1 and 6`() {
        repeat(1000) {
            val result = DiceRoller.roll()
            assertTrue("roll() returned $result, expected 1..6", result in 1..6)
        }
    }

    @Test
    fun `roll produces roughly uniform outcomes over many rolls`() {
        val random = Random(42)
        val rollCount = 600
        val counts = IntArray(6)
        repeat(rollCount) {
            counts[DiceRoller.roll(random) - 1]++
        }

        val expectedPerFace = rollCount / 6
        counts.forEachIndexed { index, count ->
            assertTrue(
                "face ${index + 1} appeared $count times, expected at least ${expectedPerFace / 2} out of $rollCount rolls",
                count >= expectedPerFace / 2
            )
        }
    }
}
