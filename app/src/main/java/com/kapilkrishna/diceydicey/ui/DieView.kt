package com.kapilkrishna.diceydicey.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kapilkrishna.diceydicey.animation.rememberRollAnimation
import com.kapilkrishna.diceydicey.domain.DiceEngine
import com.kapilkrishna.diceydicey.domain.DiceState
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.SceneView
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberOnGestureListener

private const val MODEL_ASSET_PATH = "die.v2.glb"

/**
 * cameraManipulator is explicitly null — FEATURES.md disallows drag/swipe/
 * pinch gestures, so the die should not be orbit-draggable or pinch-zoomable.
 * SceneView's default cameraManipulator is non-null (it enables those
 * gestures unless overridden), and its native rendering surface consumes
 * touch events for its own gesture detection before a Modifier.clickable
 * layered on top would ever see them — so tap handling goes through
 * SceneView's own onGestureListener instead of a Compose click modifier.
 */
@Composable
fun DieView(modifier: Modifier = Modifier) {
    val diceEngine = remember { DiceEngine() }
    var state by remember { mutableStateOf(diceEngine.state) }

    val finalFace = when (val current = state) {
        is DiceState.Idle -> current.faceValue
        is DiceState.Rolling -> current.target
    }

    val transform = rememberRollAnimation(
        isRolling = state is DiceState.Rolling,
        finalFace = finalFace
    ) {
        diceEngine.completeRoll()
        state = diceEngine.state
    }

    val renderEngine = rememberEngine()
    val modelLoader = rememberModelLoader(renderEngine)
    val modelInstance = rememberModelInstance(modelLoader, MODEL_ASSET_PATH)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        SceneView(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .aspectRatio(1f),
            engine = renderEngine,
            modelLoader = modelLoader,
            cameraManipulator = null,
            onGestureListener = rememberOnGestureListener(
                onSingleTapConfirmed = { _, _ ->
                    if (diceEngine.startRoll()) {
                        state = diceEngine.state
                    }
                }
            )
        ) {
            modelInstance?.let { instance ->
                ModelNode(
                    modelInstance = instance,
                    scaleToUnits = 1f,
                    rotation = Float3(x = transform.rotationX, y = transform.rotationY, z = 0f)
                )
            }
        }
    }
}
