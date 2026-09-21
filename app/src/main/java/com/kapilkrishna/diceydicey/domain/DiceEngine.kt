package com.kapilkrishna.diceydicey.domain

import kotlin.random.Random

/**
 * Synchronous state machine only — no timers, no coroutines. Deciding when a
 * Rolling state ends is the animation layer's job (see ARCHITECTURE.md).
 */
class DiceEngine(initialFace: Int = 1) {

    var state: DiceState = DiceState.Idle(initialFace)
        private set

    /** No-op while already Rolling. Returns true if a roll actually started. */
    fun startRoll(random: Random = Random.Default): Boolean {
        if (state is DiceState.Rolling) return false
        state = DiceState.Rolling(DiceRoller.roll(random))
        return true
    }

    /** No-op while Idle. Moves Rolling(target) to Idle(target). */
    fun completeRoll() {
        val rolling = state as? DiceState.Rolling ?: return
        state = DiceState.Idle(rolling.target)
    }
}
