package com.kapilkrishna.diceydicey.animation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Drives rememberRollAnimation on a virtual clock (autoAdvance off), so the
 * ~5 second roll is checked exactly without the test actually waiting.
 */
@RunWith(AndroidJUnit4::class)
class RollAnimationComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var isRolling by mutableStateOf(false)
    private var finalFace by mutableStateOf(1)
    private var transform = RollTransform(0f, 0f)
    private var completions = 0

    @Before
    fun setUp() {
        composeRule.mainClock.autoAdvance = false
    }

    private fun setAnimationContent() {
        composeRule.setContent {
            transform = rememberRollAnimation(isRolling, finalFace) { completions++ }
        }
        composeRule.mainClock.advanceTimeByFrame()
    }

    /** What DieView does on a tap: the engine picks a target and state flips to Rolling. */
    private fun startRoll(target: Int) {
        finalFace = target
        isRolling = true
        composeRule.mainClock.advanceTimeByFrame()
    }

    @Test
    fun idleShowsEachFacesRestingOrientation() {
        var face by mutableStateOf(1)
        composeRule.setContent {
            key(face) { transform = rememberRollAnimation(false, face) { completions++ } }
        }
        for (f in 1..6) {
            face = f
            settle()
            assertEquals("face $f", targetRotation(f), transform)
        }
        assertEquals(0, completions)
    }

    @Test
    fun idleNeverCompletesARoll() {
        setAnimationContent()
        composeRule.mainClock.advanceTimeBy(10_000)
        assertEquals(0, completions)
        assertEquals(targetRotation(1), transform)
    }

    @Test
    fun rollIsInMotionPartWayThrough() {
        setAnimationContent()
        val start = transform
        startRoll(target = 4)

        composeRule.mainClock.advanceTimeBy(1_000)

        assertNotEquals(start, transform)
        assertEquals(0, completions)
    }

    @Test
    fun rollCompletesOnceAfterAboutFiveSeconds() {
        setAnimationContent()
        startRoll(target = 4)

        composeRule.mainClock.advanceTimeBy(4_900)
        assertEquals("completed before ~5s", 0, completions)

        composeRule.mainClock.advanceTimeBy(200)
        assertEquals("did not complete by ~5.1s", 1, completions)

        composeRule.mainClock.advanceTimeBy(5_000)
        assertEquals("completed more than once", 1, completions)
    }

    @Test
    fun rollSettlesOnTheTargetFace() {
        setAnimationContent()
        // Back-to-back rolls, so each one starts from the previous roll's resting face.
        for (target in 1..6) {
            startRoll(target)
            composeRule.mainClock.advanceTimeBy(5_100)

            val expected = targetRotation(target)
            assertTrue("face $target: x=${transform.rotationX}", equivalentAngle(transform.rotationX, expected.rotationX))
            assertTrue("face $target: y=${transform.rotationY}", equivalentAngle(transform.rotationY, expected.rotationY))

            isRolling = false // what DieView does via completeRoll()
            composeRule.mainClock.advanceTimeByFrame()
        }
        assertEquals(6, completions)
    }

    @Test
    fun returningToIdleSnapsToTheExactRestingOrientation() {
        setAnimationContent()
        startRoll(target = 3)
        composeRule.mainClock.advanceTimeBy(5_100)

        isRolling = false
        settle()

        assertEquals(targetRotation(3), transform)
    }

    @Test
    fun leavingRollingEarlyCancelsWithoutCompleting() {
        setAnimationContent()
        startRoll(target = 6)
        composeRule.mainClock.advanceTimeBy(1_000)

        isRolling = false
        composeRule.mainClock.advanceTimeBy(10_000)

        assertEquals(0, completions)
        assertEquals(targetRotation(6), transform)
    }

    /** A few frames: one to relaunch the effect, one to snap, one to recompose with the result. */
    private fun settle() {
        composeRule.mainClock.advanceTimeBy(100)
    }

    private fun equivalentAngle(actual: Float, expected: Float): Boolean {
        val remainder = abs(actual - expected) % 360f
        return remainder < 0.5f || remainder > 359.5f
    }
}
