package com.tertiaryinfotech.sudokuapp.engine

/**
 * Pure Sudoku logic: full-grid generation, constraint solving, and uniqueness
 * checking. There is no UI or persistence here so the engine can be tested in
 * isolation. Grids are flat [IntArray]/[List] of length 81 where `0` marks an empty
 * cell. Ported 1:1 from the iOS `SudokuEngine`.
 */
object SudokuEngine {

    const val SIZE = 9
    const val BOX = 3
    const val CELL_COUNT = 81

    // MARK: - Coordinates

    fun row(idx: Int): Int = idx / SIZE
    fun col(idx: Int): Int = idx % SIZE
    fun boxIndex(idx: Int): Int = (row(idx) / BOX) * BOX + (col(idx) / BOX)

    /** Whether [value] can be placed at [idx] without breaking row/column/box rules. */
    fun isSafe(grid: IntArray, idx: Int, value: Int): Boolean {
        val r = idx / SIZE
        val c = idx % SIZE
        for (i in 0 until SIZE) {
            if (grid[r * SIZE + i] == value) return false   // row
            if (grid[i * SIZE + c] == value) return false   // column
        }
        val br = (r / BOX) * BOX
        val bc = (c / BOX) * BOX
        for (dr in 0 until BOX) {
            for (dc in 0 until BOX) {
                if (grid[(br + dr) * SIZE + (bc + dc)] == value) return false
            }
        }
        return true
    }

    /** Cached peer lists — indices sharing a row, column, or box with each cell. */
    private val peerTable: Array<IntArray> = Array(CELL_COUNT) { idx ->
        val r = idx / SIZE
        val c = idx % SIZE
        val set = LinkedHashSet<Int>()
        for (i in 0 until SIZE) {
            set.add(r * SIZE + i)
            set.add(i * SIZE + c)
        }
        val br = (r / BOX) * BOX
        val bc = (c / BOX) * BOX
        for (dr in 0 until BOX) {
            for (dc in 0 until BOX) set.add((br + dr) * SIZE + (bc + dc))
        }
        set.remove(idx)
        set.toIntArray()
    }

    /** Indices that share a row, column, or box with [idx] (its "peers"). */
    fun peers(idx: Int): IntArray = peerTable[idx]

    // MARK: - Solving

    /**
     * Finds the empty cell with the fewest legal candidates (minimum-remaining-values
     * heuristic). Returns `null` when the grid is full. An empty candidate list means a
     * dead end and lets the caller backtrack immediately.
     */
    private fun bestEmpty(grid: IntArray): Pair<Int, List<Int>>? {
        var best: Pair<Int, List<Int>>? = null
        for (i in 0 until CELL_COUNT) {
            if (grid[i] != 0) continue
            val candidates = ArrayList<Int>(SIZE)
            for (n in 1..SIZE) if (isSafe(grid, i, n)) candidates.add(n)
            if (candidates.size == 1) return i to candidates
            if (candidates.isEmpty()) return i to candidates
            if (best == null || candidates.size < best!!.second.size) best = i to candidates
        }
        return best
    }

    /** Returns a solved copy of [grid], or `null` if it has no solution. */
    fun solve(grid: IntArray): IntArray? {
        val g = grid.copyOf()
        return if (solveInPlace(g)) g else null
    }

    private fun solveInPlace(g: IntArray): Boolean {
        val (idx, candidates) = bestEmpty(g) ?: return true
        for (n in candidates) {
            g[idx] = n
            if (solveInPlace(g)) return true
            g[idx] = 0
        }
        return false
    }

    /** Counts solutions up to [limit]. Uniqueness only needs `limit == 2`. */
    fun solutionCount(grid: IntArray, limit: Int = 2): Int {
        val g = grid.copyOf()
        var found = intArrayOf(0)
        countSolutions(g, found, limit)
        return found[0]
    }

    private fun countSolutions(g: IntArray, found: IntArray, limit: Int) {
        if (found[0] >= limit) return
        val be = bestEmpty(g)
        if (be == null) { found[0] += 1; return }
        val (idx, candidates) = be
        for (n in candidates) {
            g[idx] = n
            countSolutions(g, found, limit)
            g[idx] = 0
            if (found[0] >= limit) return
        }
    }

    // MARK: - Generation

    /** A random, fully-solved, valid grid. */
    fun generateSolved(): IntArray {
        val g = IntArray(CELL_COUNT)
        fill(g)
        return g
    }

    private fun fill(g: IntArray): Boolean {
        val be = bestEmpty(g) ?: return true
        val (idx, candidates) = be
        if (candidates.isEmpty()) return false
        for (n in candidates.shuffled()) {
            g[idx] = n
            if (fill(g)) return true
            g[idx] = 0
        }
        return false
    }

    data class Puzzle(val puzzle: IntArray, val solution: IntArray, val clues: Int)

    /**
     * Builds a puzzle whose solution is unique by carving cells out of a solved grid.
     * Removal stops once [targetClues] givens remain (or no further cell can be removed
     * while keeping uniqueness).
     */
    fun generatePuzzle(targetClues: Int): Puzzle {
        val solution = generateSolved()
        val puzzle = solution.copyOf()
        var givens = CELL_COUNT
        for (idx in (0 until CELL_COUNT).shuffled()) {
            if (givens <= targetClues) break
            val backup = puzzle[idx]
            puzzle[idx] = 0
            if (solutionCount(puzzle, limit = 2) == 1) {
                givens -= 1
            } else {
                puzzle[idx] = backup   // removal broke uniqueness — keep the clue
            }
        }
        return Puzzle(puzzle, solution, givens)
    }
}
