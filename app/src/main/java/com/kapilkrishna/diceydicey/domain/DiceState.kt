package com.kapilkrishna.diceydicey.domain

sealed interface DiceState {
    data class Idle(val faceValue: Int) : DiceState
    data class Rolling(val target: Int) : DiceState
}
