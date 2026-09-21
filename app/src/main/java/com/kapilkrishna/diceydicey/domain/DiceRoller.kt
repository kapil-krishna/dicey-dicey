package com.kapilkrishna.diceydicey.domain

import kotlin.random.Random

object DiceRoller {
    fun roll(random: Random = Random.Default): Int = random.nextInt(1, 7)
}
