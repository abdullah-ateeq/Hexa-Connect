package com.example.hexaconnect.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.hexaconnect.game.Player

private val DarkColors = darkColorScheme(
    primary = Bronze,
    onPrimary = Midnight,
    primaryContainer = Color(0xFF3A2F20),
    onPrimaryContainer = Ivory,
    secondary = IvoryMuted,
    onSecondary = Midnight,
    background = Midnight,
    onBackground = Ivory,
    surface = Graphite,
    onSurface = Ivory,
    surfaceVariant = GraphiteRaised,
    onSurfaceVariant = IvoryMuted,
    surfaceContainerHigh = GraphiteRaised,
    surfaceContainerHighest = GraphiteRaised,
    surfaceContainer = Graphite,
    outline = GraphiteOutline,
    outlineVariant = Color(0xFF2A2E36),
    error = Color(0xFFD08A8A),
)

private val LightColors = lightColorScheme(
    primary = BronzeDeep,
    onPrimary = ParchmentRaised,
    primaryContainer = Color(0xFFEADBC2),
    onPrimaryContainer = Ink,
    secondary = InkMuted,
    onSecondary = ParchmentRaised,
    background = Parchment,
    onBackground = Ink,
    surface = ParchmentRaised,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE9E3D8),
    onSurfaceVariant = InkMuted,
    surfaceContainerHigh = Color(0xFFEFE9DF),
    surfaceContainerHighest = Color(0xFFE9E3D8),
    surfaceContainer = ParchmentRaised,
    outline = ParchmentOutline,
    outlineVariant = Color(0xFFE3DCCF),
    error = Color(0xFF9A3B3B),
)

val LocalBoardPalette = staticCompositionLocalOf { MidnightBoard }

@Composable
fun HexaConnectTheme(
    darkTheme: Boolean,
    boardPalette: BoardPalette,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalBoardPalette provides boardPalette) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = Typography,
            content = content,
        )
    }
}

/** Player colour for text and outlines, tuned for contrast against the current background. */
@Composable
fun Player.accent(): Color {
    val colors = colors()
    return if (MaterialTheme.colorScheme.background.luminance() < 0.5f) colors.light else colors.base
}
