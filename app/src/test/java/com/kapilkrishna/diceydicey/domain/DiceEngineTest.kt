package com.kapilkrishna.diceydicey.domain

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiceEngineTest {

    @Test
    fun `initial state is idle with the given face`() {
        val engine = DiceEngine(initialFace = 4)
        assertEquals(DiceState.Idle(4), engine.state)
    }

    @Test
    fun `startRoll moves from idle to rolling and returns true`() {
        val engine = DiceEngine()
        val started = engine.startRoll(Random(1))
        assertTrue(started)
        assertTrue(engine.state is DiceState.Rolling)
    }

    @Test
    fun `startRoll picks its target using the supplied Random`() {
        val engine = DiceEngine()
        engine.startRoll(Random(7))
        assertEquals(DiceState.Rolling(DiceRoller.roll(Random(7))), engine.state)
    }

    @Test
    fun `startRoll while already rolling is a no-op and returns false`() {
        val engine = DiceEngine()
        engine.startRoll(Random(1))
        val target = (engine.state as DiceState.Rolling).target

        val startedAgain = engine.startRoll(Random(2))

        assertFalse(startedAgain)
        assertEquals(target, (engine.state as DiceState.Rolling).target)
    }

    @Test
    fun `completeRoll moves rolling to idle with the target value`() {
        val engine = DiceEngine()
        engine.startRoll(Random(1))
        val target = (engine.state as DiceState.Rolling).target

        engine.completeRoll()

        assertEquals(DiceState.Idle(target), engine.state)
    }

    @Test
    fun `completeRoll while idle is a no-op`() {
        val engine = DiceEngine(initialFace = 3)
        engine.completeRoll()
        assertEquals(DiceState.Idle(3), engine.state)
    }

    @Test
    fun `after completing a roll, starting again works`() {
        val engine = DiceEngine()
        engine.startRoll(Random(1))
        engine.completeRoll()

        val startedAgain = engine.startRoll(Random(2))

        assertTrue(startedAgain)
        assertTrue(engine.state is DiceState.Rolling)
    }
}
