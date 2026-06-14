package com.tertiaryinfotech.sudokuapp.util

import com.tertiaryinfotech.sudokuapp.model.Difficulty
import kotlin.math.max

/**
 * Converts a finished game into a score. Faster solves with no hints and no
 * mistakes score highest; the result is floored so every win is worth something.
 */
object ScoreCalculator {

    const val HINT_PENALTY = 150
    const val MISTAKE_PENALTY = 100
    const val MINIMUM_SCORE = 50

    fun score(difficulty: Difficulty, seconds: Int, hintsUsed: Int, mistakes: Int): Int {
        // Beating par time earns a one-point-per-second bonus; going over par costs
        // nothing beyond losing the bonus.
        val speedBonus = max(0, difficulty.parSeconds - seconds)
        val raw = difficulty.baseScore + speedBonus -
            hintsUsed * HINT_PENALTY -
            mistakes * MISTAKE_PENALTY
        return max(MINIMUM_SCORE, raw)
    }
}
