package com.example.hexaconnect.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.hexaconnect.data.Statistics
import com.example.hexaconnect.game.Player
import com.example.hexaconnect.ui.components.HexDivider
import com.example.hexaconnect.ui.components.PrimaryButton
import com.example.hexaconnect.ui.components.SecondaryButton
import com.example.hexaconnect.ui.components.drawToken
import com.example.hexaconnect.ui.components.hexagon
import com.example.hexaconnect.ui.theme.accent
import kotlin.math.sqrt

@Composable
fun HomeScreen(
    stats: Statistics,
    onPlay: () -> Unit,
    onHowToPlay: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(
            Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            val menu: @Composable () -> Unit = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.width(280.dp),
                ) {
                    PrimaryButton("Play", onPlay, Modifier.fillMaxWidth())
                    Spacer(Modifier.height(4.dp))
                    SecondaryButton("How to Play", onHowToPlay, Modifier.fillMaxWidth())
                    SecondaryButton("Settings", onSettings, Modifier.fillMaxWidth())
                    SecondaryButton("About", onAbout, Modifier.fillMaxWidth())
                }
            }
            if (maxWidth > maxHeight) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(56.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Branding()
                        Spacer(Modifier.height(28.dp))
                        StatsRow(stats)
                    }
                    menu()
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                ) {
                    Branding()
                    Spacer(Modifier.height(44.dp))
                    menu()
                    Spacer(Modifier.height(40.dp))
                    StatsRow(stats)
                }
            }
        }
    }
}

@Composable
private fun Branding() {
    val bronze = MaterialTheme.colorScheme.primary
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Three interlocking hexes: one per player and an open cell.
        Canvas(Modifier.size(96.dp, 80.dp).clearAndSetSemantics { }) {
            val r = size.height / 3.6f
            val w = sqrt(3f) * r
            val c = Offset(size.width / 2f, size.height / 2f)
            val top = Offset(c.x, c.y - r * 0.75f)
            val left = Offset(c.x - w / 2f, c.y + r * 0.75f)
            val right = Offset(c.x + w / 2f, c.y + r * 0.75f)
            drawPath(hexagon(top, r * 0.9f), bronze, style = Stroke(1.5.dp.toPx()))
            drawToken(Player.PLAYER_ONE, left, r * 0.94f)
            drawToken(Player.PLAYER_TWO, right, r * 0.94f)
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "HEXA CONNECT",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(12.dp))
        HexDivider()
        Spacer(Modifier.height(12.dp))
        Text(
            "Build your path. Block theirs.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StatsRow(stats: Statistics) {
    Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        Stat("Games", stats.gamesPlayed.toString(), MaterialTheme.colorScheme.onBackground)
        Stat("Player 1", stats.playerOneWins.toString(), Player.PLAYER_ONE.accent())
        Stat("Player 2", stats.playerTwoWins.toString(), Player.PLAYER_TWO.accent())
    }
}

@Composable
private fun Stat(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clearAndSetSemantics {
            contentDescription = if (label == "Games") "$value games played" else "$label, $value wins"
        },
    ) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = color)
        Text(
            if (label == "Games") "PLAYED" else "${label.uppercase()} WINS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
