package com.tertiaryinfotech.sudokuapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tertiaryinfotech.sudokuapp.viewmodel.GameViewModel

/**
 * Renders the 9x9 grid: cell values, pencil notes, selection + peer highlighting,
 * conflict colouring, and the thicker 3x3 box borders.
 */
@Composable
fun BoardView(vm: GameViewModel) {
    val conflicts = if (vm.highlightConflicts) vm.conflictingIndices else emptySet()
    val selected = vm.selectedIndex
    val selectedValue = selected?.let { vm.values[it] } ?: 0

    val thin = Color(0x33808080)
    val thick = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .drawBehind {
                val cell = size.width / 9f
                // thin separators
                for (i in 0..9) {
                    val p = i * cell
                    drawLine(thin, Offset(p, 0f), Offset(p, size.height), 1f)
                    drawLine(thin, Offset(0f, p), Offset(size.width, p), 1f)
                }
                // thick 3x3 box borders
                for (i in 0..9 step 3) {
                    val p = i * cell
                    drawLine(thick, Offset(p, 0f), Offset(p, size.height), 3f)
                    drawLine(thick, Offset(0f, p), Offset(size.width, p), 3f)
                }
            }
    ) {
        for (r in 0 until 9) {
            Row(Modifier.fillMaxWidth().weight(1f)) {
                for (c in 0 until 9) {
                    val idx = r * 9 + c
                    Cell(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        value = vm.values[idx],
                        notes = vm.notes[idx],
                        isGiven = vm.isGiven[idx],
                        isSelected = selected == idx,
                        isPeer = isPeer(idx, selected),
                        isSameValue = vm.highlightSameNumber && selectedValue != 0 &&
                            vm.values[idx] == selectedValue,
                        isConflict = conflicts.contains(idx),
                        onClick = { vm.selectCell(idx) }
                    )
                }
            }
        }
    }
}

private fun isPeer(idx: Int, selected: Int?): Boolean {
    if (selected == null || selected == idx) return false
    val r1 = idx / 9; val c1 = idx % 9; val r2 = selected / 9; val c2 = selected % 9
    val sameBox = (r1 / 3 == r2 / 3) && (c1 / 3 == c2 / 3)
    return r1 == r2 || c1 == c2 || sameBox
}

@Composable
private fun Cell(
    modifier: Modifier,
    value: Int,
    notes: Set<Int>,
    isGiven: Boolean,
    isSelected: Boolean,
    isPeer: Boolean,
    isSameValue: Boolean,
    isConflict: Boolean,
    onClick: () -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    val error = MaterialTheme.colorScheme.error
    val bg = when {
        isSelected -> accent.copy(alpha = 0.28f)
        isConflict -> error.copy(alpha = 0.18f)
        isSameValue -> accent.copy(alpha = 0.14f)
        isPeer -> accent.copy(alpha = 0.07f)
        else -> Color.Transparent
    }
    val textColor = when {
        isConflict -> error
        isGiven -> MaterialTheme.colorScheme.onBackground
        else -> accent
    }
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .background(bg)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (value != 0) {
            Text(
                text = value.toString(),
                color = textColor,
                fontWeight = if (isGiven) FontWeight.Bold else FontWeight.Normal,
                fontSize = 22.sp
            )
        } else if (notes.isNotEmpty()) {
            NotesGrid(notes)
        }
    }
}

@Composable
private fun NotesGrid(notes: Set<Int>) {
    Column(Modifier.fillMaxSize()) {
        for (row in 0 until 3) {
            Row(Modifier.fillMaxWidth().weight(1f)) {
                for (col in 0 until 3) {
                    val n = row * 3 + col + 1
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (notes.contains(n)) n.toString() else "",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
