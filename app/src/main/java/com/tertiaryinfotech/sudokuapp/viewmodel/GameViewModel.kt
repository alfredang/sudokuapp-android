package com.tertiaryinfotech.sudokuapp.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tertiaryinfotech.sudokuapp.data.ScoreStore
import com.tertiaryinfotech.sudokuapp.engine.SudokuEngine
import com.tertiaryinfotech.sudokuapp.model.ActiveGame
import com.tertiaryinfotech.sudokuapp.model.AppScreen
import com.tertiaryinfotech.sudokuapp.model.Difficulty
import com.tertiaryinfotech.sudokuapp.model.GameAlert
import com.tertiaryinfotech.sudokuapp.model.GameSession
import com.tertiaryinfotech.sudokuapp.util.ScoreCalculator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * The single source of truth for the running game and all navigation. Holds the
 * board, the timer, hint/mistake bookkeeping, settings, and persists both the
 * in-progress game and finished sessions through [ScoreStore]. Ported from the iOS
 * `GameViewModel`; Compose snapshot state replaces `@Published`.
 */
class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val store = ScoreStore(app)
    private val prefs = app.getSharedPreferences("SudokuApp", Context.MODE_PRIVATE)

    // MARK: Navigation
    var screen by mutableStateOf(AppScreen.HOME)
    var activeAlert by mutableStateOf<GameAlert?>(null)

    // MARK: 18+ gate
    var isAgeConfirmed by mutableStateOf(prefs.getBoolean(Keys.AGE_CONFIRMED, false))
        private set

    fun confirmAge() {
        isAgeConfirmed = true
        prefs.edit().putBoolean(Keys.AGE_CONFIRMED, true).apply()
    }

    // MARK: Board state
    var difficulty by mutableStateOf(Difficulty.EASY)
        private set
    var puzzle by mutableStateOf(List(81) { 0 })       // givens
        private set
    var solution by mutableStateOf(List(81) { 0 })
        private set
    var values by mutableStateOf(List(81) { 0 })       // current entries
        private set
    var notes by mutableStateOf(List(81) { emptySet<Int>() })
        private set

    var selectedIndex by mutableStateOf<Int?>(null)
    var isNotesMode by mutableStateOf(false)
        private set
    var mistakes by mutableStateOf(0)
        private set
    var hintsUsed by mutableStateOf(0)
        private set
    var elapsedSeconds by mutableStateOf(0)
        private set
    var isPaused by mutableStateOf(false)
        private set

    // MARK: Settings (persisted). Default ON, except the optional mistake limit.
    var highlightConflicts by mutableStateOf(prefs.getBoolean(Keys.HL_CONFLICTS, true))
        private set
    var highlightSameNumber by mutableStateOf(prefs.getBoolean(Keys.HL_SAME, true))
        private set
    var autoRemoveNotes by mutableStateOf(prefs.getBoolean(Keys.AUTO_NOTES, true))
        private set
    var limitMistakes by mutableStateOf(prefs.getBoolean(Keys.LIMIT_MISTAKES, false))
        private set

    val mistakeLimit = 3

    fun updateHighlightConflicts(v: Boolean) { highlightConflicts = v; prefs.edit().putBoolean(Keys.HL_CONFLICTS, v).apply() }
    fun updateHighlightSameNumber(v: Boolean) { highlightSameNumber = v; prefs.edit().putBoolean(Keys.HL_SAME, v).apply() }
    fun updateAutoRemoveNotes(v: Boolean) { autoRemoveNotes = v; prefs.edit().putBoolean(Keys.AUTO_NOTES, v).apply() }
    fun updateLimitMistakes(v: Boolean) { limitMistakes = v; prefs.edit().putBoolean(Keys.LIMIT_MISTAKES, v).apply() }

    // MARK: Stored result of the last finished game (for the completion screen)
    var lastSession by mutableStateOf<GameSession?>(null)
        private set

    private var timerJob: Job? = null
    private val undoStack = ArrayDeque<UndoEntry>()

    private data class UndoEntry(val index: Int, val value: Int, val notes: Set<Int>)

    private object Keys {
        const val AGE_CONFIRMED = "SudokuApp.ageConfirmed"
        const val HL_CONFLICTS = "SudokuApp.highlightConflicts"
        const val HL_SAME = "SudokuApp.highlightSameNumber"
        const val AUTO_NOTES = "SudokuApp.autoRemoveNotes"
        const val LIMIT_MISTAKES = "SudokuApp.limitMistakes"
    }

    // MARK: - Derived state

    val hasResumableGame: Boolean get() = store.hasActiveGame()

    val isGiven: List<Boolean> get() = puzzle.map { it != 0 }

    val isSolved: Boolean get() = values == solution

    /** Remaining count for each digit 1..9 (how many still to place on the board). */
    fun remaining(digit: Int): Int = 9 - values.count { it == digit }

    /** Indices that currently violate a Sudoku rule (duplicate in row/col/box). */
    val conflictingIndices: Set<Int>
        get() {
            val conflicts = HashSet<Int>()
            for (i in 0 until 81) {
                if (values[i] == 0) continue
                for (p in SudokuEngine.peers(i)) {
                    if (values[p] == values[i]) { conflicts.add(i); conflicts.add(p) }
                }
            }
            return conflicts
        }

    val formattedElapsed: String
        get() = "%02d:%02d".format(elapsedSeconds / 60, elapsedSeconds % 60)

    // MARK: - Game lifecycle

    fun startNewGame(level: Difficulty) {
        val generated = SudokuEngine.generatePuzzle(level.targetClues)
        difficulty = level
        puzzle = generated.puzzle.toList()
        solution = generated.solution.toList()
        values = generated.puzzle.toList()
        notes = List(81) { emptySet() }
        selectedIndex = null
        isNotesMode = false
        mistakes = 0
        hintsUsed = 0
        elapsedSeconds = 0
        isPaused = false
        undoStack.clear()
        screen = AppScreen.GAME
        persistActiveGame()
        startTimer()
    }

    fun resumeSavedGame() {
        val game = store.loadActiveGame() ?: return
        difficulty = game.difficulty
        puzzle = game.puzzle
        solution = game.solution
        values = game.values
        notes = game.notes.map { it.toSet() }
        selectedIndex = null
        isNotesMode = false
        mistakes = game.mistakes
        hintsUsed = game.hintsUsed
        elapsedSeconds = game.elapsedSeconds
        isPaused = false
        undoStack.clear()
        screen = AppScreen.GAME
        startTimer()
    }

    fun quitToHome() {
        stopTimer()
        persistActiveGame()
        selectedIndex = null
        screen = AppScreen.HOME
    }

    fun goHome() {
        screen = AppScreen.HOME
    }

    // MARK: - Player input

    fun selectCell(index: Int) {
        selectedIndex = if (selectedIndex == index) null else index
    }

    fun enter(digit: Int) {
        val idx = selectedIndex ?: return
        if (isGiven[idx] || isPaused) return
        recordUndo(idx)

        val newValues = values.toMutableList()
        val newNotes = notes.toMutableList()

        if (isNotesMode) {
            val cell = newNotes[idx].toMutableSet()
            if (cell.contains(digit)) cell.remove(digit) else cell.add(digit)
            newNotes[idx] = cell
            newValues[idx] = 0
        } else {
            // Tapping the same digit again clears the cell.
            if (newValues[idx] == digit) {
                newValues[idx] = 0
            } else {
                newValues[idx] = digit
                newNotes[idx] = emptySet()
            }
        }
        values = newValues
        notes = newNotes

        if (!isNotesMode && values[idx] == digit) {
            registerMistakeIfNeeded(idx, digit)
            if (autoRemoveNotes) pruneNotes(idx, digit)
        }
        persistActiveGame()
        checkForWin()
    }

    fun erase() {
        val idx = selectedIndex ?: return
        if (isGiven[idx] || isPaused) return
        recordUndo(idx)
        values = values.toMutableList().also { it[idx] = 0 }
        notes = notes.toMutableList().also { it[idx] = emptySet() }
        persistActiveGame()
    }

    fun toggleNotesMode() { isNotesMode = !isNotesMode }

    fun undo() {
        val last = undoStack.removeLastOrNull() ?: return
        values = values.toMutableList().also { it[last.index] = last.value }
        notes = notes.toMutableList().also { it[last.index] = last.notes }
        persistActiveGame()
    }

    /**
     * Reveals the correct value for the selected empty cell (or, if none is selected,
     * the first empty cell). Counts toward the hint tally and lowers the final score.
     */
    fun useHint() {
        if (isPaused) return
        val sel = selectedIndex
        val target = if (sel != null && !isGiven[sel] && values[sel] != solution[sel]) sel
        else firstUnsolvedIndex()
        val idx = target ?: return
        recordUndo(idx)
        values = values.toMutableList().also { it[idx] = solution[idx] }
        notes = notes.toMutableList().also { it[idx] = emptySet() }
        if (autoRemoveNotes) pruneNotes(idx, solution[idx])
        hintsUsed += 1
        selectedIndex = idx
        persistActiveGame()
        checkForWin()
    }

    fun togglePause() {
        if (screen != AppScreen.GAME || isSolved) return
        isPaused = !isPaused
        if (isPaused) stopTimer() else startTimer()
    }

    // MARK: - Win / loss handling

    private fun checkForWin() {
        if (!isSolved) return
        stopTimer()
        val score = ScoreCalculator.score(difficulty, elapsedSeconds, hintsUsed, mistakes)
        val session = GameSession(
            id = UUID.randomUUID().toString(),
            difficulty = difficulty,
            dateEpochSeconds = System.currentTimeMillis() / 1000,
            durationSeconds = elapsedSeconds,
            hintsUsed = hintsUsed,
            mistakes = mistakes,
            score = score
        )
        store.save(session)
        store.clearActiveGame()
        lastSession = session
        screen = AppScreen.COMPLETION
    }

    private fun registerMistakeIfNeeded(idx: Int, digit: Int) {
        if (digit == solution[idx]) return
        mistakes += 1
        if (limitMistakes && mistakes >= mistakeLimit) {
            stopTimer()
            activeAlert = GameAlert(
                title = "Out of moves",
                message = "You reached $mistakeLimit mistakes. Start a new game to try again."
            )
        }
    }

    private fun firstUnsolvedIndex(): Int? =
        (0 until 81).firstOrNull { !isGiven[it] && values[it] != solution[it] }

    // MARK: - Helpers

    private fun recordUndo(idx: Int) {
        undoStack.addLast(UndoEntry(idx, values[idx], notes[idx]))
        if (undoStack.size > 100) undoStack.removeFirst()
    }

    /** Removes [digit] from the pencil marks of the cell's peers. */
    private fun pruneNotes(idx: Int, digit: Int) {
        val newNotes = notes.toMutableList()
        var changed = false
        for (p in SudokuEngine.peers(idx)) {
            if (newNotes[p].contains(digit)) {
                newNotes[p] = newNotes[p] - digit
                changed = true
            }
        }
        if (changed) notes = newNotes
    }

    private fun persistActiveGame() {
        if (screen != AppScreen.GAME || isSolved) return
        store.saveActiveGame(
            ActiveGame(
                difficulty = difficulty,
                puzzle = puzzle,
                solution = solution,
                values = values,
                notes = notes.map { it.sorted() },
                elapsedSeconds = elapsedSeconds,
                mistakes = mistakes,
                hintsUsed = hintsUsed
            )
        )
    }

    // MARK: - Timer

    private fun startTimer() {
        stopTimer()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (screen != AppScreen.GAME || isPaused || isSolved) continue
                elapsedSeconds += 1
                if (elapsedSeconds % 10 == 0) persistActiveGame()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    /** Auto-pause when the app leaves the foreground so the timer stays honest. */
    fun onEnterBackground() {
        if (screen == AppScreen.GAME && !isPaused && !isSolved) togglePause()
    }
}
