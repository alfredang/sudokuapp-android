package com.tertiaryinfotech.sudokuapp.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

/**
 * The four selectable difficulty levels. Each level maps to a target number of
 * givens (clues) plus the scoring constants used when a puzzle is completed.
 */
@Serializable
enum class Difficulty {
    EASY, MEDIUM, HARD, EXPERT;

    val title: String
        get() = when (this) {
            EASY -> "Easy"
            MEDIUM -> "Medium"
            HARD -> "Hard"
            EXPERT -> "Expert"
        }

    val subtitle: String
        get() = when (this) {
            EASY -> "Gentle warm-up"
            MEDIUM -> "A balanced challenge"
            HARD -> "For seasoned solvers"
            EXPERT -> "Ruthless. Good luck."
        }

    val icon: ImageVector
        get() = when (this) {
            EASY -> Icons.Filled.Eco
            MEDIUM -> Icons.Filled.Whatshot
            HARD -> Icons.Filled.Bolt
            EXPERT -> Icons.Filled.WorkspacePremium
        }

    val tint: Color
        get() = when (this) {
            EASY -> Color(0xFF34C759)
            MEDIUM -> Color(0xFF0A84FF)
            HARD -> Color(0xFFFF9500)
            EXPERT -> Color(0xFFAF52DE)
        }

    /** Target number of starting clues. Fewer clues => harder puzzle. */
    val targetClues: Int
        get() = when (this) {
            EASY -> 45
            MEDIUM -> 36
            HARD -> 30
            EXPERT -> 25
        }

    /** "Par" solve time in seconds — beating it earns a speed bonus. */
    val parSeconds: Int
        get() = when (this) {
            EASY -> 300
            MEDIUM -> 600
            HARD -> 900
            EXPERT -> 1500
        }

    /** Base points awarded for completing a puzzle of this level. */
    val baseScore: Int
        get() = when (this) {
            EASY -> 1000
            MEDIUM -> 2000
            HARD -> 3500
            EXPERT -> 5000
        }
}
