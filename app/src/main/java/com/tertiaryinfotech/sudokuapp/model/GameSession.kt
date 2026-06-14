package com.tertiaryinfotech.sudokuapp.model

import kotlinx.serialization.Serializable

/** A completed game, persisted to local storage for the score history and stats. */
@Serializable
data class GameSession(
    val id: String,
    val difficulty: Difficulty,
    val dateEpochSeconds: Long,
    val durationSeconds: Int,
    val hintsUsed: Int,
    val mistakes: Int,
    val score: Int
) {
    /** `mm:ss` (or `h:mm:ss`) formatting of the solve time. */
    val formattedDuration: String
        get() {
            val h = durationSeconds / 3600
            val m = (durationSeconds % 3600) / 60
            val s = durationSeconds % 60
            return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
        }
}

/**
 * A snapshot of an in-progress game so the player can quit and resume later.
 * Stored separately from finished sessions and cleared once the puzzle is solved.
 */
@Serializable
data class ActiveGame(
    val difficulty: Difficulty,
    val puzzle: List<Int>,        // starting givens (0 = blank)
    val solution: List<Int>,      // unique solution
    val values: List<Int>,        // current entries (0 = blank)
    val notes: List<List<Int>>,   // pencil marks per cell
    val elapsedSeconds: Int,
    val mistakes: Int,
    val hintsUsed: Int
)
