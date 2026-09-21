package com.kapilkrishna.diceydicey.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kapilkrishna.diceydicey.domain.DiceEngine
import com.kapilkrishna.diceydicey.domain.DiceState
import com.kapilkrishna.diceydicey.domain.DieFace

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
        Text(
            text = DieFace.fromInt(faceValue).pips.toString(),
            style = MaterialTheme.typography.displayLarge
        )
    }
}
