package com.kapilkrishna.diceydicey.animation

import kotlin.math.abs
import kotlin.math.round
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RollAnimationTest {

    @Test
    fun `targetRotation matches the verified face-to-orientation table`() {
        // Verified on-device for all six faces — see ARCHITECTURE.md § 3D Die Model.
        assertEquals(RollTransform(rotationX = 0f, rotationY = 90f), targetRotation(1))
        assertEquals(RollTransform(rotationX = 180f, rotationY = 0f), targetRotation(2))
        assertEquals(RollTransform(rotationX = -90f, rotationY = 0f), targetRotation(3))
        assertEquals(RollTransform(rotationX = 90f, rotationY = 0f), targetRotation(4))
        assertEquals(RollTransform(rotationX = 0f, rotationY = 0f), targetRotation(5))
        assertEquals(RollTransform(rotationX = 0f, rotationY = -90f), targetRotation(6))
    }

    @Test
    fun `targetRotation gives each face a distinct orientation`() {
        val orientations = (1..6).map { targetRotation(it) }.toSet()
        assertEquals(6, orientations.size)
    }

    @Test(expected = IllegalStateException::class)
    fun `targetRotation rejects a face below 1`() {
        targetRotation(0)
    }

    @Test(expected = IllegalStateException::class)
    fun `targetRotation rejects a face above 6`() {
        targetRotation(7)
    }

    @Test
    fun `spinTo always lands on an angle equivalent to the target`() {
        val currents = listOf(0f, 90f, -90f, 180f, 1234f, -2519f, 3600f)
        val targets = (1..6).flatMap { targetRotation(it).let { t -> listOf(t.rotationX, t.rotationY) } }.toSet()

        for (seed in 0 until 50) {
            val random = Random(seed)
            for (current in currents) {
                for (target in targets) {
                    val result = spinTo(current, target, random)
                    assertTrue(
                        "spinTo($current, $target) = $result is not equivalent to $target mod 360",
                        isMultipleOf360(result - target)
                    )
                }
            }
        }
    }

    @Test
    fun `spinTo adds 4 to 7 full turns in either direction from the nearest target angle`() {
        val current = 1234f
        val target = 90f
        val nearest = 1170f // the angle ≡ 90 (mod 360) closest to 1234

        val turnCounts = (0 until 500).map { seed ->
            val result = spinTo(current, target, Random(seed))
            round((result - nearest) / 360f).toInt()
        }.toSet()

        // Every value in range is reachable, both directions happen, and nothing falls outside.
        assertEquals(setOf(-7, -6, -5, -4, 4, 5, 6, 7), turnCounts)
    }

    @Test
    fun `spinTo continues from the current angle rather than resetting to zero`() {
        val current = 3600f + 10f // ten turns in from the previous roll
        val result = spinTo(current, 0f, Random(3))
        val turnsTravelled = abs(result - current) / 360f
        assertTrue("travelled $turnsTravelled turns, expected about 4 to 7", turnsTravelled in 3.9f..7.1f)
    }

    @Test
    fun `spinTo is deterministic for the same Random seed`() {
        assertEquals(spinTo(45f, 90f, Random(9)), spinTo(45f, 90f, Random(9)))
    }

    private fun isMultipleOf360(value: Float): Boolean {
        val remainder = abs(value) % 360f
        return remainder < 0.01f || remainder > 359.99f
    }
}
