package com.example.hexaconnect.game

/**
 * Pure game rules. Holds the current [HexaConnectState] and never touches Android or UI code.
 *
 * Player 1 connects the top row to the bottom row.
 * Player 2 connects the left column to the right column.
 */
class HexaConnectGame(initial: HexaConnectState = HexaConnectState.initial()) {

    var state: HexaConnectState = initial
        private set

    val size: Int get() = state.size

    fun getNeighbors(cell: HexCell): List<HexCell> = HexGrid.getNeighbors(cell, size)

    /**
     * Claims [cell] for the current player. Returns false and leaves the state unchanged if the
     * move is illegal: game already won, input locked by an animation, cell outside the board
     * or cell already claimed.
     */
    fun placePiece(cell: HexCell): Boolean {
        val current = state
        if (current.winner != null) return false
        if (current.isAnimating) return false
        if (!HexGrid.isInside(cell, size)) return false
        if (current.ownerOf(cell) != null) return false

        val player = current.currentPlayer
        val board = current.board.mapIndexed { r, row ->
            if (r == cell.row) row.mapIndexed { c, owner -> if (c == cell.col) player else owner } else row
        }
        val claimed = current.copy(
            board = board,
            moveHistory = current.moveHistory + cell,
            lastMove = cell,
        )
        state = claimed

        val path = findWinningPath(player)
        state = if (path != null) {
            claimed.copy(winner = player, winningPath = path)
        } else {
            claimed.copy(currentPlayer = player.opponent)
        }
        return true
    }

    /**
     * Breadth-first search through [player]'s cells from their starting edge to their target edge.
     * Returns the cells of the shortest connecting chain in order from start edge to target edge,
     * or null if the player has no connection.
     */
    fun findWinningPath(player: Player): List<HexCell>? {
        val board = state.board
        val last = size - 1
        val starts = (0 until size)
            .map { i -> if (player == Player.PLAYER_ONE) HexCell(0, i) else HexCell(i, 0) }
            .filter { board[it.row][it.col] == player }
        val isTarget: (HexCell) -> Boolean =
            if (player == Player.PLAYER_ONE) { c -> c.row == last } else { c -> c.col == last }

        val queue = ArrayDeque(starts)
        val visited = starts.toHashSet()
        val parent = HashMap<HexCell, HexCell>()

        while (queue.isNotEmpty()) {
            val cell = queue.removeFirst()
            if (isTarget(cell)) return reconstruct(cell, parent)
            for (next in HexGrid.getNeighbors(cell, size)) {
                if (board[next.row][next.col] == player && visited.add(next)) {
                    parent[next] = cell
                    queue.addLast(next)
                }
            }
        }
        return null
    }

    /** Reverts the most recent placement. Returns false if there is nothing to undo or input is locked. */
    fun undo(): Boolean {
        val current = state
        if (current.isAnimating || current.moveHistory.isEmpty()) return false
        val cell = current.moveHistory.last()
        val player = current.ownerOf(cell) ?: return false
        val history = current.moveHistory.dropLast(1)
        val board = current.board.mapIndexed { r, row ->
            if (r == cell.row) row.mapIndexed { c, owner -> if (c == cell.col) null else owner } else row
        }
        state = current.copy(
            board = board,
            currentPlayer = player,
            lastMove = history.lastOrNull(),
            winningPath = emptyList(),
            winner = null,
            moveHistory = history,
        )
        return true
    }

    fun setAnimating(animating: Boolean) {
        state = state.copy(isAnimating = animating)
    }

    fun restart() {
        state = HexaConnectState.initial(size)
    }

    private fun reconstruct(end: HexCell, parent: Map<HexCell, HexCell>): List<HexCell> {
        val path = ArrayList<HexCell>()
        var cell: HexCell? = end
        while (cell != null) {
            path.add(cell)
            cell = parent[cell]
        }
        path.reverse()
        return path
    }
}
