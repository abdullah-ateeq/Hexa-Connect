package com.example.hexaconnect.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.hexaconnect.data.BoardTheme
import com.example.hexaconnect.game.Player

// App chrome
val Midnight = Color(0xFF101217)
val Graphite = Color(0xFF181B21)
val GraphiteRaised = Color(0xFF21252D)
val GraphiteOutline = Color(0xFF363B45)
val Bronze = Color(0xFFB8955F)
val BronzeDeep = Color(0xFF8A6A3A)
val Ivory = Color(0xFFECE6DA)
val IvoryMuted = Color(0xFFA7A194)

val Parchment = Color(0xFFF2EEE6)
val ParchmentRaised = Color(0xFFFAF7F1)
val ParchmentOutline = Color(0xFFD8D0C2)
val Ink = Color(0xFF1D1F24)
val InkMuted = Color(0xFF5E5A52)

/** Colours for one player's pieces. */
data class PlayerColors(
    val base: Color,
    val light: Color,
    val dark: Color,
    val symbol: Color,
)

val PlayerOneColors = PlayerColors(
    base = Color(0xFF8E2F3D),
    light = Color(0xFFB54D5C),
    dark = Color(0xFF5A1B26),
    symbol = Color(0xFFF3DADD),
)

val PlayerTwoColors = PlayerColors(
    base = Color(0xFF1E5E6C),
    light = Color(0xFF34879A),
    dark = Color(0xFF113D47),
    symbol = Color(0xFFD5EDF1),
)

fun Player.colors(): PlayerColors = if (this == Player.PLAYER_ONE) PlayerOneColors else PlayerTwoColors

/** Colours for the board surface, independent of the light/dark app theme. */
data class BoardPalette(
    val plate: Color,
    val plateEdge: Color,
    val cellTop: Color,
    val cellBottom: Color,
    val cellStroke: Color,
    val highlight: Color,
    val shadow: Color,
)

val ClassicBoard = BoardPalette(
    plate = Color(0xFF3B342D),
    plateEdge = Color(0xFF9C7B4B),
    cellTop = Color(0xFFDCD3C3),
    cellBottom = Color(0xFFC3B7A2),
    cellStroke = Color(0xFF8F7148),
    highlight = Color(0x66FFFFFF),
    shadow = Color(0x55000000),
)

val MidnightBoard = BoardPalette(
    plate = Color(0xFF0E1218),
    plateEdge = Color(0xFF7D6440),
    cellTop = Color(0xFF2D3542),
    cellBottom = Color(0xFF212833),
    cellStroke = Color(0xFF8A6D45),
    highlight = Color(0x22FFFFFF),
    shadow = Color(0x88000000),
)

fun BoardTheme.palette(): BoardPalette = when (this) {
    BoardTheme.CLASSIC -> ClassicBoard
    BoardTheme.MIDNIGHT -> MidnightBoard
}
