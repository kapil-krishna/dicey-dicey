package com.kapilkrishna.diceydicey.domain

enum class DieFace(val pips: Int) {
    ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6);

    companion object {
        fun fromInt(value: Int): DieFace =
            entries.first { it.pips == value }
    }
}

private val TOP_LEFT = 0 to 0
private val TOP_RIGHT = 0 to 2
private val MIDDLE_LEFT = 1 to 0
private val CENTER = 1 to 1
private val MIDDLE_RIGHT = 1 to 2
private val BOTTOM_LEFT = 2 to 0
private val BOTTOM_RIGHT = 2 to 2

private val DOT_POSITIONS: Map<DieFace, Set<Pair<Int, Int>>> = mapOf(
    DieFace.ONE to setOf(CENTER),
    DieFace.TWO to setOf(TOP_RIGHT, BOTTOM_LEFT),
    DieFace.THREE to setOf(TOP_RIGHT, CENTER, BOTTOM_LEFT),
    DieFace.FOUR to setOf(TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT),
    DieFace.FIVE to setOf(TOP_LEFT, TOP_RIGHT, CENTER, BOTTOM_LEFT, BOTTOM_RIGHT),
    DieFace.SIX to setOf(TOP_LEFT, MIDDLE_LEFT, BOTTOM_LEFT, TOP_RIGHT, MIDDLE_RIGHT, BOTTOM_RIGHT),
)

/** 3x3 grid, outer list = rows top-to-bottom, inner list = columns left-to-right. */
fun DieFace.dotGrid(): List<List<Boolean>> {
    val activeDots = DOT_POSITIONS.getValue(this)
    return (0..2).map { row ->
        (0..2).map { col -> (row to col) in activeDots }
    }
}
