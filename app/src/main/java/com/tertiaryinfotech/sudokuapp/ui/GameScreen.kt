package com.tertiaryinfotech.sudokuapp.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tertiaryinfotech.sudokuapp.viewmodel.GameViewModel

/**
 * The playing screen: status bar, the board, the number pad, and the action row
 * (undo, erase, notes, hint).
 */
@Composable
fun GameScreen(vm: GameViewModel) {
    var showQuitConfirm by remember { mutableStateOf(false) }

    BackHandler { showQuitConfirm = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatusBar(vm, onBack = { showQuitConfirm = true })
        BoardArea(vm)
        Spacer(Modifier.weight(1f))
        ActionRow(vm)
        NumberPad(vm)
    }

    if (showQuitConfirm) {
        AlertDialog(
            onDismissRequest = { showQuitConfirm = false },
            title = { Text("Leave this game?") },
            text = { Text("Your progress is saved so you can continue later.") },
            confirmButton = {
                TextButton(onClick = { showQuitConfirm = false; vm.quitToHome() }) {
                    Text("Save & Quit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitConfirm = false }) { Text("Keep Playing") }
            }
        )
    }
}

@Composable
private fun StatusBar(vm: GameViewModel, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(vm.difficulty.icon, contentDescription = null, tint = vm.difficulty.tint, modifier = Modifier.size(18.dp))
            Text(
                vm.difficulty.title,
                color = vm.difficulty.tint,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            if (vm.mistakes > 0 || vm.limitMistakes) {
                val text = if (vm.limitMistakes) "${vm.mistakes}/${vm.mistakeLimit}" else "${vm.mistakes}"
                Text("✕ $text", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleSmall)
            }
            Row(
                modifier = Modifier.clickable { vm.togglePause() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    if (vm.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                    contentDescription = "Pause",
                    modifier = Modifier.size(18.dp)
                )
                Text(vm.formattedElapsed, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun BoardArea(vm: GameViewModel) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp)
                .blur(if (vm.isPaused) 14.dp else 0.dp)
        ) {
            BoardView(vm)
        }
        if (vm.isPaused) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Filled.PauseCircleFilled, contentDescription = null, modifier = Modifier.size(50.dp))
                Text("Paused", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Button(onClick = { vm.togglePause() }) { Text("Resume") }
            }
        }
    }
}

@Composable
private fun ActionRow(vm: GameViewModel) {
    Row(Modifier.fillMaxWidth()) {
        ActionButton("Undo", Icons.AutoMirrored.Filled.Undo, Modifier.weight(1f)) { vm.undo() }
        ActionButton("Erase", Icons.Filled.Backspace, Modifier.weight(1f)) { vm.erase() }
        ActionButton(
            "Notes",
            Icons.Filled.Edit,
            Modifier.weight(1f),
            highlighted = vm.isNotesMode
        ) { vm.toggleNotesMode() }
        ActionButton(
            "Hint",
            Icons.Filled.Lightbulb,
            Modifier.weight(1f),
            badge = if (vm.hintsUsed > 0) vm.hintsUsed.toString() else null
        ) { vm.useHint() }
    }
}

@Composable
private fun ActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    badge: String? = null,
    onClick: () -> Unit
) {
    val tint = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
    Column(
        modifier = modifier.clickable(onClick = onClick).padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        BadgedBox(badge = { if (badge != null) Badge { Text(badge) } }) {
            Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(26.dp))
        }
        Text(title, style = MaterialTheme.typography.labelMedium, color = tint)
    }
}

@Composable
private fun NumberPad(vm: GameViewModel) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (digit in 1..9) {
            val remaining = vm.remaining(digit)
            val enabled = remaining != 0
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(enabled = enabled) { vm.enter(digit) }
                    .padding(vertical = 10.dp)
                    .then(if (enabled) Modifier else Modifier),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    digit.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                )
                Text(
                    if (remaining > 0) remaining.toString() else " ",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
