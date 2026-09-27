package com.example.hexaconnect.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.hexaconnect.game.HexCell
import com.example.hexaconnect.game.Player
import com.example.hexaconnect.ui.board.BoardFrame
import com.example.hexaconnect.ui.board.HexGeometry
import com.example.hexaconnect.ui.board.drawHexBoard
import com.example.hexaconnect.ui.components.HexDivider
import com.example.hexaconnect.ui.theme.LocalBoardPalette

private const val SPLASH_N = 7
private const val SPLASH_MS = 1_450

private val SPLASH_PATH = listOf(
    HexCell(0, 3), HexCell(1, 3), HexCell(2, 2), HexCell(3, 2), HexCell(4, 1), HexCell(5, 1), HexCell(6, 1),
)
private val SPLASH_BLOCKERS = listOf(
    HexCell(2, 3), HexCell(3, 1), HexCell(1, 4), HexCell(4, 2), HexCell(5, 0), HexCell(3, 4),
)

/** Interleaved move order: Player 1 path cell, then a Player 2 blocker, ending on the connecting move. */
private val SPLASH_MOVES: List<Pair<HexCell, Player>> = buildList {
    SPLASH_PATH.forEachIndexed { i, cell ->
        add(cell to Player.PLAYER_ONE)
        SPLASH_BLOCKERS.getOrNull(i)?.let { add(it to Player.PLAYER_TWO) }
    }
}

private fun phase(t: Float, start: Float, end: Float) = ((t - start) / (end - start)).coerceIn(0f, 1f)

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }
    val finish by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(SPLASH_MS, easing = LinearEasing))
        finish()
    }
    val palette = LocalBoardPalette.current
    val owners = remember { SPLASH_MOVES.toMap() }
    val order = remember { SPLASH_MOVES.mapIndexed { i, (cell, _) -> cell to i }.toMap() }
    val pathSet = remember { SPLASH_PATH.toSet() }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.semantics { contentDescription = "Hexa Connect" }) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Canvas(
                    Modifier
                        .widthIn(max = 360.dp)
                        .fillMaxWidth(0.82f)
                        .aspectRatio(1.45f)
                        .graphicsLayer { alpha = phase(progress.value, 0f, 0.18f) },
                ) {
                    val t = progress.value
                    val fillStart = 0.12f
                    val fillEnd = 0.66f
                    val step = (fillEnd - fillStart) / SPLASH_MOVES.size
                    val win = phase(t, 0.68f, 0.84f)
                    drawHexBoard(
                        HexGeometry(SPLASH_N, size),
                        BoardFrame(
                            owner = { cell ->
                                val i = order[cell]
                                if (i != null && t >= fillStart + i * step) owners[cell] else null
                            },
                            palette = palette,
                            fillProgress = { cell ->
                                val i = order[cell] ?: return@BoardFrame 1f
                                phase(t, fillStart + i * step, fillStart + (i + 1.4f) * step)
                            },
                            winner = if (win > 0f) Player.PLAYER_ONE else null,
                            winningPath = if (win > 0f) pathSet else emptySet(),
                            winProgress = win,
                            railGlow = if (t < 0.86f) win else 1f - 0.5f * phase(t, 0.86f, 1f),
                        ),
                    )
                }
                Spacer(Modifier.height(28.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer {
                        val a = phase(progress.value, 0.6f, 0.88f)
                        alpha = a
                        translationY = (1f - a) * 12.dp.toPx()
                    },
                ) {
                    Text(
                        "HEXA CONNECT",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(12.dp))
                    HexDivider()
                }
            }
        }
    }
}
