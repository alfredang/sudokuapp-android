package com.tertiaryinfotech.sudokuapp.data

import android.content.Context
import android.content.SharedPreferences
import com.tertiaryinfotech.sudokuapp.model.ActiveGame
import com.tertiaryinfotech.sudokuapp.model.Difficulty
import com.tertiaryinfotech.sudokuapp.model.GameSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Lightweight on-device persistence of completed games and the in-progress game,
 * stored as JSON in [SharedPreferences]. Kept deliberately simple — no Room — and
 * nothing ever leaves the device. Mirrors the iOS `ScoreStore`.
 */
class ScoreStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("SudokuApp", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val sessionsKey = "SudokuApp.sessions"
    private val activeKey = "SudokuApp.activeGame"
    private val maxStored = 200

    // MARK: - Completed sessions

    /** All saved games, most recent first. */
    fun allSessions(): List<GameSession> {
        val data = prefs.getString(sessionsKey, null) ?: return emptyList()
        val sessions = runCatching { json.decodeFromString<List<GameSession>>(data) }
            .getOrDefault(emptyList())
        return sessions.sortedByDescending { it.dateEpochSeconds }
    }

    /** Appends a finished game and trims history to [maxStored]. */
    fun save(session: GameSession) {
        val sessions = allSessions().toMutableList()
        sessions.removeAll { it.id == session.id }
        sessions.add(0, session)
        persistSessions(sessions.take(maxStored))
    }

    fun delete(id: String) {
        persistSessions(allSessions().filter { it.id != id })
    }

    fun clearSessions() {
        prefs.edit().remove(sessionsKey).apply()
    }

    private fun persistSessions(sessions: List<GameSession>) {
        prefs.edit().putString(sessionsKey, json.encodeToString(sessions)).apply()
    }

    // MARK: - Active (resumable) game

    fun loadActiveGame(): ActiveGame? {
        val data = prefs.getString(activeKey, null) ?: return null
        return runCatching { json.decodeFromString<ActiveGame>(data) }.getOrNull()
    }

    fun saveActiveGame(game: ActiveGame) {
        prefs.edit().putString(activeKey, json.encodeToString(game)).apply()
    }

    fun clearActiveGame() {
        prefs.edit().remove(activeKey).apply()
    }

    fun hasActiveGame(): Boolean = prefs.contains(activeKey)

    // MARK: - Derived stats

    data class Stats(
        val gamesPlayed: Int,
        val totalScore: Int,
        val bestScore: Int,
        val bestTimeByDifficulty: Map<Difficulty, Int>,
        val countByDifficulty: Map<Difficulty, Int>
    )

    fun stats(): Stats {
        val sessions = allSessions()
        val bestTime = HashMap<Difficulty, Int>()
        val counts = HashMap<Difficulty, Int>()
        for (s in sessions) {
            counts[s.difficulty] = (counts[s.difficulty] ?: 0) + 1
            val existing = bestTime[s.difficulty]
            bestTime[s.difficulty] =
                if (existing != null) minOf(existing, s.durationSeconds) else s.durationSeconds
        }
        return Stats(
            gamesPlayed = sessions.size,
            totalScore = sessions.sumOf { it.score },
            bestScore = sessions.maxOfOrNull { it.score } ?: 0,
            bestTimeByDifficulty = bestTime,
            countByDifficulty = counts
        )
    }
}
