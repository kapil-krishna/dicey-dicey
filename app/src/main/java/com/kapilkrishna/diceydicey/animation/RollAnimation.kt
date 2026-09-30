package com.kapilkrishna.diceydicey.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val ROLL_DURATION_MS = 5000

data class RollTransform(val rotationX: Float, val rotationY: Float)

/**
 * The Euler rotation (x/y degrees) that shows [faceValue] face-up, derived by
 * analyzing die.v2.glb's actual mesh geometry (grouping the "Dot" material's
 * geometry into 21 connected components and classifying each by nearest local
 * axis) — see ARCHITECTURE.md's "Face-value -> orientation mapping" table.
 * Assumes the camera faces the model along -Z; if the wrong face shows at
 * rest on-device, that assumption (or the rotation sign convention) is what
 * to revisit — see the note in ARCHITECTURE.md.
 */
internal fun targetRotation(faceValue: Int): RollTransform = when (faceValue) {
    1 -> RollTransform(rotationX = 0f, rotationY = 90f)
    2 -> RollTransform(rotationX = 180f, rotationY = 0f)
    3 -> RollTransform(rotationX = -90f, rotationY = 0f)
    4 -> RollTransform(rotationX = 90f, rotationY = 0f)
    5 -> RollTransform(rotationX = 0f, rotationY = 0f)
    6 -> RollTransform(rotationX = 0f, rotationY = -90f)
    else -> error("Invalid face value: $faceValue")
}

/**
 * Drives the die's roll as one continuous motion per axis — several extra
 * full spins that decelerate smoothly into the exact orientation for
 * [finalFace] (a "throw and tumble to rest," not a fast spin followed by a
 * separately-eased settle — that two-phase design had a jarring speed
 * discontinuity right where they met) — then calls [onRollComplete]. The die
 * stays in place; only rotation is animated. The ~5 second duration lives
 * here, not in the domain layer — see ARCHITECTURE.md's decision on who owns
 * roll timing.
 */
@Composable
fun rememberRollAnimation(
    isRolling: Boolean,
    finalFace: Int,
    onRollComplete: () -> Unit
): RollTransform {
    val rotationX = remember { Animatable(0f) }
    val rotationY = remember { Animatable(0f) }

    LaunchedEffect(isRolling) {
        if (!isRolling) {
            val target = targetRotation(finalFace)
            rotationX.snapTo(target.rotationX)
            rotationY.snapTo(target.rotationY)
            return@LaunchedEffect
        }

        val target = targetRotation(finalFace)
        val finalX = spinTo(rotationX.value, target.rotationX)
        val finalY = spinTo(rotationY.value, target.rotationY)

        coroutineScope {
            launch {
                rotationX.animateTo(finalX, tween(ROLL_DURATION_MS, easing = LinearOutSlowInEasing))
            }
            launch {
                rotationY.animateTo(finalY, tween(ROLL_DURATION_MS, easing = LinearOutSlowInEasing))
            }
        }
        onRollComplete()
    }

    return RollTransform(rotationX.value, rotationY.value)
}

/**
 * A few extra full turns beyond [target] (congruent to it modulo 360, so it
 * still lands exactly on the correct face), continuing smoothly from
 * [current] rather than resetting to 0 between rolls.
 */
internal fun spinTo(current: Float, target: Float, random: Random = Random.Default): Float {
    val nearest = target + kotlin.math.round((current - target) / 360f) * 360f
    val direction = if (random.nextBoolean()) 1 else -1
    val spins = random.nextInt(4, 8)
    return nearest + direction * spins * 360f
}
