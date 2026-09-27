package com.example.hexaconnect.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.hexaconnect.game.Player
import com.example.hexaconnect.ui.theme.colors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

val ButtonShape = RoundedCornerShape(6.dp)

/** Full-screen scaffold for the text screens: header with back chevron and a centred, scrollable column. */
@Composable
fun DetailScreen(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
            ) {
                BackButton(onBack)
                Text(
                    title.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() },
                )
            }
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    content = content,
                )
            }
        }
    }
}

@Composable
fun BackButton(onBack: () -> Unit) {
    val color = MaterialTheme.colorScheme.onBackground
    IconButton(onClick = onBack, modifier = Modifier.semantics { contentDescription = "Back" }) {
        Canvas(Modifier.size(20.dp)) {
            val w = size.width
            val path = Path().apply {
                moveTo(w * 0.62f, w * 0.18f)
                lineTo(w * 0.3f, w * 0.5f)
                lineTo(w * 0.62f, w * 0.82f)
            }
            drawPath(path, color, style = Stroke(w * 0.09f, cap = StrokeCap.Round))
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = ButtonShape,
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = modifier.heightIn(min = 56.dp),
    ) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    compact: Boolean = false,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = ButtonShape,
        border = BorderStroke(
            1.dp,
            if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        ),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
        contentPadding = if (compact) PaddingValues(horizontal = 12.dp, vertical = 10.dp)
        else PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        modifier = modifier.heightIn(min = if (compact) 44.dp else 52.dp),
    ) {
        Text(
            text.uppercase(),
            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

/** A player's piece rendered as a small bevelled hex with its symbol. */
@Composable
fun PlayerToken(player: Player, size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) { drawToken(player, center, this.size.minDimension / 2f) }
}

fun DrawScope.drawToken(player: Player, c: Offset, r: Float) {
    val colors = player.colors()
    val hex = hexagon(c, r * 0.96f)
    drawPath(hex, Brush.verticalGradient(listOf(colors.light, colors.base, colors.dark), c.y - r, c.y + r))
    drawPath(hexagon(c, r * 0.86f), Color.White.copy(alpha = 0.16f), style = Stroke(r * 0.06f))
    when (player) {
        Player.PLAYER_ONE -> drawPath(
            Path().apply {
                moveTo(c.x, c.y - r * 0.34f)
                lineTo(c.x + r * 0.25f, c.y)
                lineTo(c.x, c.y + r * 0.34f)
                lineTo(c.x - r * 0.25f, c.y)
                close()
            },
            colors.symbol,
        )
        Player.PLAYER_TWO -> drawCircle(colors.symbol, radius = r * 0.25f, center = c)
    }
}

fun hexagon(c: Offset, r: Float): Path = Path().apply {
    for (i in 0 until 6) {
        val a = (PI / 180.0 * (60 * i - 30)).toFloat()
        val x = c.x + r * cos(a)
        val y = c.y + r * sin(a)
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

/** Small hex bullet used in lists. */
@Composable
fun HexBullet(color: Color = MaterialTheme.colorScheme.primary) {
    Canvas(Modifier.size(10.dp)) { drawPath(hexagon(center, size.minDimension / 2f), color) }
}

@Composable
fun BulletRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        HexBullet()
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun SectionLabel(text: String) {
    Spacer(Modifier.height(4.dp))
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.semantics { heading() },
    )
}

/** Thin bronze rule with a centred hex, used as a divider on branded screens. */
@Composable
fun HexDivider(modifier: Modifier = Modifier, width: Dp = 120.dp) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier.size(width, 10.dp)) {
        val y = size.height / 2f
        val gap = size.height
        drawLine(color.copy(alpha = 0.5f), Offset(0f, y), Offset(size.width / 2f - gap, y), strokeWidth = 1.dp.toPx())
        drawLine(color.copy(alpha = 0.5f), Offset(size.width / 2f + gap, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        drawPath(hexagon(Offset(size.width / 2f, y), size.height / 2.4f), color)
    }
}
