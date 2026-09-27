package com.example.hexaconnect.ui.screens

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hexaconnect.audio.Sound
import com.example.hexaconnect.audio.SoundEffects
import com.example.hexaconnect.data.Settings
import com.example.hexaconnect.game.HexaConnectState
import com.example.hexaconnect.game.Player
import com.example.hexaconnect.ui.board.HexBoard
import com.example.hexaconnect.ui.board.objectiveDescription
import com.example.hexaconnect.ui.components.PlayerToken
import com.example.hexaconnect.ui.components.SecondaryButton
import com.example.hexaconnect.ui.theme.LocalBoardPalette
import com.example.hexaconnect.ui.theme.accent
import com.example.hexaconnect.ui.theme.colors

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    settings: Settings,
    sounds: SoundEffects?,
    onFinished: (MatchResult) -> Unit,
    onHome: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val view = LocalView.current
    val currentSettings by rememberUpdatedState(settings)
    val currentOnFinished by rememberUpdatedState(onFinished)
    var confirmRestart by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val s = currentSettings
            fun play(sound: Sound) { if (s.soundEnabled) sounds?.play(sound) }
            when (event) {
                GameEvent.Placed -> {
                    play(Sound.PLACE)
                    if (s.hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
                GameEvent.TurnChanged -> play(Sound.TURN)
                GameEvent.PathComplete -> {
                    play(Sound.CONNECT)
                    if (s.hapticsEnabled) {
                        val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            HapticFeedbackConstants.CONFIRM
                        } else {
                            HapticFeedbackConstants.LONG_PRESS
                        }
                        view.performHapticFeedback(effect)
                    }
                }
                is GameEvent.Finished -> {
                    play(Sound.WIN)
                    currentOnFinished(event.result)
                }
            }
        }
    }

    val palette = LocalBoardPalette.current
    val board: @Composable (Modifier) -> Unit = { modifier ->
        Box(modifier, contentAlignment = Alignment.Center) {
            HexBoard(
                state = state,
                palette = palette,
                showLastMove = settings.showLastMove,
                showRails = settings.showObjectiveBorders,
                onCellTap = viewModel::onCellTapped,
                modifier = Modifier.fillMaxSize(),
            )
            Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)) {
                PathCompleteBanner(state.winner)
            }
        }
    }
    val controls: @Composable (Modifier) -> Unit = { modifier ->
        Row(modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton("Undo", viewModel::undo, Modifier.weight(1f), enabled = state.canUndo, compact = true)
            SecondaryButton(
                "Restart",
                { confirmRestart = true },
                Modifier.weight(1f),
                enabled = state.winner == null && state.moveHistory.isNotEmpty(),
                compact = true,
            )
            SecondaryButton("Menu", onHome, Modifier.weight(1f), compact = true)
        }
    }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(
            Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            if (maxWidth > maxHeight) {
                Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    board(Modifier.weight(1f).fillMaxHeight())
                    Spacer(Modifier.width(20.dp))
                    Column(
                        Modifier.width(300.dp).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                    ) {
                        GameHeader(state)
                        PlayerPanel(Player.PLAYER_TWO, state)
                        TurnIndicator(state, Modifier.fillMaxWidth())
                        PlayerPanel(Player.PLAYER_ONE, state)
                        Spacer(Modifier.height(4.dp))
                        controls(Modifier.fillMaxWidth())
                    }
                }
            } else {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GameHeader(state)
                    PlayerPanel(Player.PLAYER_TWO, state)
                    board(Modifier.weight(1f).fillMaxWidth())
                    TurnIndicator(state, Modifier.fillMaxWidth())
                    PlayerPanel(Player.PLAYER_ONE, state)
                    controls(Modifier.fillMaxWidth())
                }
            }
        }
    }

    if (confirmRestart) {
        AlertDialog(
            onDismissRequest = { confirmRestart = false },
            title = { Text("Restart this match?") },
            text = { Text("The current board will be cleared.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmRestart = false
                    viewModel.restart()
                }) { Text("Restart") }
            },
            dismissButton = {
                TextButton(onClick = { confirmRestart = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun GameHeader(state: HexaConnectState) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            "HEXA CONNECT",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        Text(
            "MOVE ${state.moveHistory.size + if (state.winner == null) 1 else 0}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PlayerPanel(player: Player, state: HexaConnectState) {
    val active = state.winner == null && state.currentPlayer == player
    val isWinner = state.winner == player
    val highlighted = active || isWinner
    val colors = player.colors()
    val emphasis by animateFloatAsState(if (highlighted) 1f else 0f, tween(180), label = "panel")
    val objective = if (player == Player.PLAYER_ONE) "Top ↔ Bottom" else "Left ↔ Right"
    val status = when {
        isWinner -> "Winner"
        active -> "To move"
        else -> ""
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (highlighted) colors.light.copy(alpha = 0.35f + 0.45f * emphasis)
            else MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription = buildString {
                    append(player.objectiveDescription())
                    if (status.isNotEmpty()) append(", $status")
                }
            },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            PlayerToken(player, 30.dp, Modifier.alpha(0.55f + 0.45f * emphasis))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "PLAYER ${player.number}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f + 0.4f * emphasis),
                )
                Text(
                    objective,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (status.isNotEmpty()) {
                Text(
                    status.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = player.accent(),
                )
            }
        }
    }
}

@Composable
private fun TurnIndicator(state: HexaConnectState, modifier: Modifier = Modifier) {
    val winner = state.winner
    val player = winner ?: state.currentPlayer
    val title = if (winner != null) "PLAYER ${winner.number} WINS" else "PLAYER ${player.number}'S TURN"
    val objective = if (player == Player.PLAYER_ONE) "Connect Top ↔ Bottom" else "Connect Left ↔ Right"
    Column(
        modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = player.accent())
        Text(objective, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PathCompleteBanner(winner: Player?) {
    AnimatedVisibility(
        visible = winner != null,
        enter = fadeIn(tween(260, delayMillis = 350)) + scaleIn(tween(260, delayMillis = 350), initialScale = 0.94f),
        exit = fadeOut(tween(120)),
    ) {
        val colors = (winner ?: Player.PLAYER_ONE).colors()
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            border = BorderStroke(1.dp, colors.light.copy(alpha = 0.7f)),
            shadowElevation = 8.dp,
        ) {
            Text(
                "PATH COMPLETE",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 14.dp),
            )
        }
    }
}
