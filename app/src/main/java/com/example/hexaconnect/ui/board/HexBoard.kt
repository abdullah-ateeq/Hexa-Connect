package com.example.hexaconnect.ui.board

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import com.example.hexaconnect.game.HexCell
import com.example.hexaconnect.game.HexaConnectState
import com.example.hexaconnect.game.Player
import com.example.hexaconnect.ui.theme.BoardPalette
import kotlin.math.roundToInt

const val PLACE_ANIMATION_MS = 150
private const val WIN_REVEAL_MS = 650

@Composable
fun HexBoard(
    state: HexaConnectState,
    palette: BoardPalette,
    showLastMove: Boolean,
    showRails: Boolean,
    onCellTap: (HexCell) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentState by rememberUpdatedState(state)
    val currentOnTap by rememberUpdatedState(onCellTap)
    var preview by remember { mutableStateOf<HexCell?>(null) }

    // Placement fill animation, restarted for every new move.
    val fill = remember { Animatable(1f) }
    val moveCount = state.moveHistory.size
    var previousCount by remember { mutableStateOf(moveCount) }
    LaunchedEffect(moveCount) {
        if (moveCount > previousCount) {
            fill.snapTo(0f)
            fill.animateTo(1f, tween(PLACE_ANIMATION_MS, easing = FastOutSlowInEasing))
        } else {
            fill.snapTo(1f)
        }
        previousCount = moveCount
    }

    val win = remember { Animatable(0f) }
    val glow = remember { Animatable(0f) }
    LaunchedEffect(state.winner) {
        if (state.winner != null) {
            win.animateTo(1f, tween(WIN_REVEAL_MS, delayMillis = PLACE_ANIMATION_MS, easing = FastOutSlowInEasing))
            glow.animateTo(1f, tween(260, easing = LinearEasing))
            glow.animateTo(0.55f, tween(520, easing = FastOutSlowInEasing))
        } else {
            win.snapTo(0f)
            glow.snapTo(0f)
        }
    }

    BoxWithConstraints(modifier) {
        val density = LocalDensity.current
        val canvasSize = with(density) { Size(maxWidth.toPx(), maxHeight.toPx()) }
        val geometry = remember(canvasSize, state.size) { HexGeometry(state.size, canvasSize) }
        val winningSet = remember(state.winningPath) { state.winningPath.toSet() }

        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(geometry) {
                    awaitPointerEventScope {
                        var pressed = false
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: continue
                            val s = currentState
                            val interactive = s.winner == null && !s.isAnimating
                            val hit = geometry.hitTest(change.position)
                                ?.takeIf { interactive && s.ownerOf(it) == null }
                            when (event.type) {
                                PointerEventType.Press -> {
                                    pressed = true
                                    preview = hit
                                    change.consume()
                                }
                                PointerEventType.Move -> {
                                    // Drag while pressed, or mouse hover.
                                    if (pressed || change.type == PointerType.Mouse) preview = hit
                                }
                                PointerEventType.Release -> {
                                    if (pressed && hit != null) currentOnTap(hit)
                                    pressed = false
                                    preview = null
                                    change.consume()
                                }
                                PointerEventType.Exit -> preview = null
                                else -> Unit
                            }
                        }
                    }
                },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val last = state.lastMove
                drawHexBoard(
                    geometry,
                    BoardFrame(
                        owner = state::ownerOf,
                        palette = palette,
                        showRails = showRails,
                        lastMove = if (showLastMove) last else null,
                        preview = preview,
                        previewPlayer = state.currentPlayer,
                        fillProgress = { cell -> if (cell == last) fill.value else 1f },
                        winner = state.winner,
                        winningPath = winningSet,
                        winProgress = win.value,
                        railGlow = glow.value,
                    ),
                )
            }

            CellSemantics(geometry, state, winningSet, onCellTap)
        }
    }
}

/** Invisible per-cell nodes so TalkBack can explore and activate individual hexes. */
@Composable
private fun CellSemantics(
    geometry: HexGeometry,
    state: HexaConnectState,
    winningSet: Set<HexCell>,
    onCellTap: (HexCell) -> Unit,
) {
    val density = LocalDensity.current
    val cellSize = with(density) { (geometry.hexWidth).toDp() }
    val half = geometry.hexWidth / 2f
    for (row in 0 until state.size) for (col in 0 until state.size) {
        val cell = HexCell(row, col)
        val center = geometry.center(cell)
        val owner = state.ownerOf(cell)
        val label = buildString {
            append("Row ${row + 1} column ${col + 1}, ")
            append(
                when {
                    cell in winningSet -> "Winning path cell, Player ${owner?.number}"
                    owner == null -> "Empty hex"
                    else -> "Player ${owner.number}"
                },
            )
        }
        Box(
            Modifier
                .offset { IntOffset((center.x - half).roundToInt(), (center.y - half).roundToInt()) }
                .size(cellSize)
                .clearAndSetSemantics {
                    contentDescription = label
                    if (owner == null && state.winner == null) {
                        onClick(label = "Claim hex") {
                            onCellTap(cell)
                            true
                        }
                    }
                },
        )
    }
}

fun Player.objectiveDescription(): String =
    if (this == Player.PLAYER_ONE) "Player 1 connects top to bottom" else "Player 2 connects left to right"
