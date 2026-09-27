package com.example.hexaconnect.ui.board

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import com.example.hexaconnect.game.HexCell
import com.example.hexaconnect.game.Player
import com.example.hexaconnect.ui.theme.BoardPalette
import com.example.hexaconnect.ui.theme.colors

/** Everything the renderer needs to paint one frame of a board. Pure data, no game logic. */
class BoardFrame(
    val owner: (HexCell) -> Player?,
    val palette: BoardPalette,
    val showRails: Boolean = true,
    val lastMove: HexCell? = null,
    val preview: HexCell? = null,
    val previewPlayer: Player? = null,
    /** 0..1 fill progress for a cell; 1 means fully placed. */
    val fillProgress: (HexCell) -> Float = { 1f },
    val winner: Player? = null,
    val winningPath: Set<HexCell> = emptySet(),
    /** 0..1 progress of the win reveal (path brightens, others dim). */
    val winProgress: Float = 0f,
    /** 0..1 extra glow on the winner's target rails. */
    val railGlow: Float = 0f,
)

private const val CELL_SCALE = 0.9f

fun DrawScope.drawHexBoard(g: HexGeometry, frame: BoardFrame) {
    val r = g.radius
    if (r <= 0f) return
    val palette = frame.palette

    // Backing plate with a soft drop shadow.
    val outline = g.boardOutline()
    val margin = r * 0.42f
    translate(top = r * 0.18f) {
        drawPath(outline, palette.shadow, style = Fill)
        drawPath(outline, palette.shadow, style = Stroke(margin * 2f, join = StrokeJoin.Round))
    }
    drawPath(outline, palette.plate, style = Fill)
    drawPath(outline, palette.plate, style = Stroke(margin * 2f, join = StrokeJoin.Round))

    if (frame.showRails) drawRails(g, frame)

    for (row in 0 until g.n) for (col in 0 until g.n) {
        drawCell(g, HexCell(row, col), frame)
    }
}

private fun DrawScope.drawRails(g: HexGeometry, frame: BoardFrame) {
    val r = g.radius
    val width = r * 0.34f
    fun rail(points: List<Offset>, player: Player) {
        val path = polyline(points)
        val colors = player.colors()
        if (frame.winner == player && frame.railGlow > 0f) {
            drawPath(
                path,
                colors.light.copy(alpha = 0.28f * frame.railGlow),
                style = Stroke(width + r * 0.7f * frame.railGlow, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
        val base = if (frame.winner == player) lerp(colors.base, colors.light, frame.railGlow) else colors.base
        val dim = if (frame.winner != null && frame.winner != player) 1f - 0.45f * frame.winProgress else 1f
        drawPath(
            path,
            base.copy(alpha = 0.92f * dim),
            style = Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
    rail(g.leftRail(), Player.PLAYER_TWO)
    rail(g.rightRail(), Player.PLAYER_TWO)
    rail(g.topRail(), Player.PLAYER_ONE)
    rail(g.bottomRail(), Player.PLAYER_ONE)
}

private fun DrawScope.drawCell(g: HexGeometry, cell: HexCell, frame: BoardFrame) {
    val r = g.radius
    val palette = frame.palette
    val c = g.center(cell)
    val cellR = r * CELL_SCALE
    val hex = g.hexPath(c, cellR)
    val top = c.y - cellR
    val bottom = c.y + cellR

    // Matte stone base with a slight bevel.
    drawPath(g.hexPath(c + Offset(0f, r * 0.07f), cellR), palette.shadow)
    drawPath(hex, Brush.verticalGradient(listOf(palette.cellTop, palette.cellBottom), top, bottom))
    drawPath(hex, palette.cellStroke.copy(alpha = 0.35f), style = Stroke(r * 0.045f))
    drawPath(g.hexPath(c, cellR - r * 0.06f), palette.highlight, style = Stroke(r * 0.035f))

    val owner = frame.owner(cell)
    val onPath = cell in frame.winningPath
    if (owner != null) {
        val t = frame.fillProgress(cell).coerceIn(0f, 1f)
        if (t > 0f) drawPiece(g, c, owner, t, if (onPath) frame.winProgress else 0f)
    } else if (cell == frame.preview && frame.previewPlayer != null) {
        val colors = frame.previewPlayer.colors()
        drawPath(hex, colors.base.copy(alpha = 0.32f))
        drawSymbol(frame.previewPlayer, c, r, colors.symbol.copy(alpha = 0.55f), outline = true)
    }

    if (frame.winner != null && !onPath && frame.winProgress > 0f) {
        drawPath(hex, palette.plate.copy(alpha = 0.5f * frame.winProgress))
    }

    if (cell == frame.lastMove && owner != null) {
        val ring = owner.colors().symbol.copy(alpha = 0.8f * frame.fillProgress(cell))
        drawCircle(ring, radius = r * 0.56f, center = c, style = Stroke(r * 0.055f))
    }
}

private fun DrawScope.drawPiece(g: HexGeometry, c: Offset, player: Player, t: Float, highlight: Float) {
    val r = g.radius
    val colors = player.colors()
    val scale = 0.55f + 0.45f * t
    val pieceR = r * CELL_SCALE * scale
    val light = lerp(colors.light, Color.White, 0.18f * highlight)
    val base = lerp(colors.base, colors.light, 0.55f * highlight)
    val piece = g.hexPath(c, pieceR)
    drawPath(
        piece,
        Brush.verticalGradient(listOf(light, base, colors.dark), c.y - pieceR, c.y + pieceR),
        alpha = t,
    )
    drawPath(g.hexPath(c, pieceR - r * 0.07f), Color.White.copy(alpha = 0.16f * t), style = Stroke(r * 0.05f))
    drawPath(piece, colors.dark.copy(alpha = 0.6f * t), style = Stroke(r * 0.04f))
    if (highlight > 0f) {
        drawPath(piece, colors.symbol.copy(alpha = 0.65f * highlight), style = Stroke(r * 0.09f))
    }
    drawSymbol(player, c, r * scale, colors.symbol.copy(alpha = 0.92f * t), outline = false)
}

private fun DrawScope.drawSymbol(player: Player, c: Offset, r: Float, color: Color, outline: Boolean) {
    val style = if (outline) Stroke(r * 0.06f) else Fill
    when (player) {
        Player.PLAYER_ONE -> {
            val path = Path().apply {
                moveTo(c.x, c.y - r * 0.3f)
                lineTo(c.x + r * 0.22f, c.y)
                lineTo(c.x, c.y + r * 0.3f)
                lineTo(c.x - r * 0.22f, c.y)
                close()
            }
            drawPath(path, color, style = style)
        }
        Player.PLAYER_TWO -> drawCircle(color, radius = r * 0.22f, center = c, style = style)
    }
}

private fun polyline(points: List<Offset>): Path = Path().apply {
    if (points.isEmpty()) return@apply
    moveTo(points[0].x, points[0].y)
    for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
}
