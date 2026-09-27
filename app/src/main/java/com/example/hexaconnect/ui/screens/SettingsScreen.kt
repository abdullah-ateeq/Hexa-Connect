package com.example.hexaconnect.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.hexaconnect.data.BoardTheme
import com.example.hexaconnect.data.Settings
import com.example.hexaconnect.data.Statistics
import com.example.hexaconnect.data.ThemeMode
import com.example.hexaconnect.ui.components.DetailScreen
import com.example.hexaconnect.ui.components.SecondaryButton
import com.example.hexaconnect.ui.components.SectionLabel

@Composable
fun SettingsScreen(
    settings: Settings,
    stats: Statistics,
    viewModel: AppViewModel,
    onBack: () -> Unit,
) {
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    DetailScreen(title = "Settings", onBack = onBack) {
        SectionLabel("Appearance")
        SettingsGroup {
            ChoiceRow(
                label = "Theme",
                options = listOf(ThemeMode.LIGHT to "Light", ThemeMode.DARK to "Dark", ThemeMode.SYSTEM to "System"),
                selected = settings.themeMode,
                onSelect = viewModel::setThemeMode,
            )
            Divider()
            ChoiceRow(
                label = "Board Theme",
                options = listOf(BoardTheme.CLASSIC to "Classic", BoardTheme.MIDNIGHT to "Midnight"),
                selected = settings.boardTheme,
                onSelect = viewModel::setBoardTheme,
            )
        }

        SectionLabel("Gameplay")
        SettingsGroup {
            ToggleRow("Sound Effects", settings.soundEnabled, viewModel::setSoundEnabled)
            Divider()
            ToggleRow("Haptic Feedback", settings.hapticsEnabled, viewModel::setHapticsEnabled)
            Divider()
            ToggleRow("Show Last Move", settings.showLastMove, viewModel::setShowLastMove)
            Divider()
            ToggleRow("Show Objective Borders", settings.showObjectiveBorders, viewModel::setShowObjectiveBorders)
        }

        SectionLabel("Statistics")
        SettingsGroup {
            StatLine("Games Played", stats.gamesPlayed.toString())
            Divider()
            StatLine("Player 1 Wins", stats.playerOneWins.toString())
            Divider()
            StatLine("Player 2 Wins", stats.playerTwoWins.toString())
            Divider()
            StatLine("Shortest Winning Path", stats.shortestPath?.let { "$it cells" } ?: "—")
        }
        SecondaryButton(
            "Reset Statistics",
            { confirmReset = true },
            Modifier.fillMaxWidth(),
            enabled = stats.gamesPlayed > 0,
        )
        Spacer(Modifier.padding(4.dp))
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset statistics?") },
            text = { Text("Games played and win counts will be cleared.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    viewModel.resetStatistics()
                }) { Text("Reset") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 16.dp)) { content() }
    }
}

@Composable
private fun Divider() = HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            ),
        )
    }
}

@Composable
private fun <T> ChoiceRow(label: String, options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    Column(Modifier.padding(vertical = 12.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.padding(top = 10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .selectableGroup(),
        ) {
            options.forEachIndexed { index, (value, text) ->
                val isSelected = value == selected
                Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(value) }),
                ) {
                    Text(
                        text.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 13.dp),
                    )
                }
                if (index < options.lastIndex) Spacer(Modifier.width(2.dp))
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
