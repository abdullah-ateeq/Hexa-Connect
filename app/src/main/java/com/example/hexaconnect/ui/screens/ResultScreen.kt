package com.example.hexaconnect.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.hexaconnect.game.BOARD_SIZE
import com.example.hexaconnect.game.Player
import com.example.hexaconnect.ui.components.HexDivider
import com.example.hexaconnect.ui.components.PlayerToken
import com.example.hexaconnect.ui.components.PrimaryButton
import com.example.hexaconnect.ui.components.SecondaryButton
import com.example.hexaconnect.ui.theme.accent
import kotlin.math.roundToInt

@Composable
fun ResultScreen(result: MatchResult, onPlayAgain: () -> Unit, onHome: () -> Unit) {
    val winner = result.winner
    val coverage = (result.moves * 100f / (BOARD_SIZE * BOARD_SIZE)).roundToInt()
    val seconds = result.durationMs / 1000
    val duration = "%d:%02d".format(seconds / 60, seconds % 60)

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Box(
            Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
            ) {
                Spacer(Modifier.height(24.dp))
                PlayerToken(winner, 64.dp)
                Spacer(Modifier.height(20.dp))
                Text(
                    "PLAYER ${winner.number} WINS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = winner.accent(),
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (winner == Player.PLAYER_ONE) "Top and bottom connected" else "Left and right connected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                HexDivider()
                Spacer(Modifier.height(24.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        ResultRow("Moves Played", result.moves.toString())
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        ResultRow("Path Length", "${result.pathLength} cells")
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        ResultRow("Match Duration", duration)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        ResultRow("Board Coverage", "$coverage%")
                    }
                }
                Spacer(Modifier.height(32.dp))
                PrimaryButton("Play Again", onPlayAgain, Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                SecondaryButton("Home", onHome, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
            .semantics(mergeDescendants = true) {},
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
