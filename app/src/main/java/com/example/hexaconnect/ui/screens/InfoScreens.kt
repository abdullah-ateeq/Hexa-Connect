package com.example.hexaconnect.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.hexaconnect.game.HexCell
import com.example.hexaconnect.game.Player
import com.example.hexaconnect.ui.board.BoardFrame
import com.example.hexaconnect.ui.board.HexGeometry
import com.example.hexaconnect.ui.board.drawHexBoard
import com.example.hexaconnect.ui.components.BulletRow
import com.example.hexaconnect.ui.components.DetailScreen
import com.example.hexaconnect.ui.components.HexDivider
import com.example.hexaconnect.ui.components.PlayerToken
import com.example.hexaconnect.ui.components.SectionLabel
import com.example.hexaconnect.ui.theme.LocalBoardPalette
import com.example.hexaconnect.ui.theme.accent

@Composable
fun HowToPlayScreen(onBack: () -> Unit) {
    DetailScreen(title = "How to Play", onBack = onBack) {
        MiniBoardDiagram()
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxWidth()) {
            ObjectiveCard(Player.PLAYER_ONE, "Top ↔ Bottom", Modifier.weight(1f))
            ObjectiveCard(Player.PLAYER_TWO, "Left ↔ Right", Modifier.weight(1f))
        }
        SectionLabel("Rules")
        BulletRow("Players take turns placing one piece on any empty hex.")
        BulletRow("Pieces never move and can never be captured.")
        BulletRow("Player 1 connects the top edge to the bottom edge.")
        BulletRow("Player 2 connects the left edge to the right edge.")
        BulletRow("Paths can bend through any of a hex's six neighbours.")
        BulletRow("Block your opponent while extending your own route.")
        BulletRow("The first complete path wins. There are no draws.")
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ObjectiveCard(player: Player, objective: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
            PlayerToken(player, 26.dp)
            Spacer(Modifier.padding(start = 10.dp))
            Column {
                Text("PLAYER ${player.number}", style = MaterialTheme.typography.labelMedium, color = player.accent())
                Text(objective, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

/** A 5×5 example where Player 1 has bent a path around Player 2's blockers. */
@Composable
private fun MiniBoardDiagram() {
    val palette = LocalBoardPalette.current
    val path = remember { listOf(HexCell(0, 2), HexCell(1, 2), HexCell(2, 1), HexCell(3, 1), HexCell(4, 0)) }
    val owners = remember {
        path.associateWith { Player.PLAYER_ONE } +
            listOf(HexCell(1, 1), HexCell(2, 2), HexCell(3, 2), HexCell(2, 0)).associateWith { Player.PLAYER_TWO }
    }
    Canvas(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1.6f)
            .semantics {
                contentDescription =
                    "Example board: Player 1 bends a path from top to bottom around Player 2's pieces"
            },
    ) {
        drawHexBoard(
            HexGeometry(5, size),
            BoardFrame(
                owner = { owners[it] },
                palette = palette,
                winner = Player.PLAYER_ONE,
                winningPath = path.toSet(),
                winProgress = 0.6f,
                railGlow = 0.4f,
            ),
        )
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    DetailScreen(title = "About", onBack = onBack) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlayerToken(Player.PLAYER_ONE, 36.dp)
                PlayerToken(Player.PLAYER_TWO, 36.dp)
            }
            Spacer(Modifier.height(16.dp))
            Text("Hexa Connect", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(4.dp))
            Text("Version 1.0", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            HexDivider()
        }
        Text(
            "A polished local two-player connection strategy game where each player races to build a " +
                "continuous path between opposite sides of a hexagonal board.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SectionLabel("Features")
        BulletRow("Local 2 Player")
        BulletRow("9×9 Hex Board")
        BulletRow("Connection Strategy")
        BulletRow("Offline Play")
        BulletRow("Premium Board Themes")
    }
}
