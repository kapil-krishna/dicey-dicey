package com.kapilkrishna.diceydicey.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.kapilkrishna.diceydicey.domain.DiceEngine
import com.kapilkrishna.diceydicey.domain.DiceState
import com.kapilkrishna.diceydicey.domain.DieFace
import com.kapilkrishna.diceydicey.domain.dotGrid

private const val GRID_SIZE = 3
private val FaceColor = Color.White
private val DotColor = Color.Black

/**
 * No animation yet (Phase 7 adds that) — a tap starts and completes a roll
 * immediately so the state wiring can be verified on its own.
 */
@Composable
fun DieView(modifier: Modifier = Modifier) {
    val engine = remember { DiceEngine() }
    var state by remember { mutableStateOf(engine.state) }

    val faceValue = when (val current = state) {
        is DiceState.Idle -> current.faceValue
        is DiceState.Rolling -> current.target
    }
    val face = DieFace.fromInt(faceValue)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable {
                if (engine.startRoll()) {
                    engine.completeRoll()
                    state = engine.state
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .aspectRatio(1f)
        ) {
            drawDieFace(face)
        }
    }
}

private fun DrawScope.drawDieFace(face: DieFace) {
    val cornerRadius = CornerRadius(size.minDimension * 0.12f)
    drawRoundRect(color = FaceColor, cornerRadius = cornerRadius)

    val cellSize = size.minDimension / GRID_SIZE
    val dotRadius = cellSize * 0.18f

    face.dotGrid().forEachIndexed { row, cells ->
        cells.forEachIndexed { col, isLit ->
            if (isLit) {
                drawCircle(
                    color = DotColor,
                    radius = dotRadius,
                    center = Offset(
                        x = cellSize * col + cellSize / 2f,
                        y = cellSize * row + cellSize / 2f
                    )
                )
            }
        }
    }
}
