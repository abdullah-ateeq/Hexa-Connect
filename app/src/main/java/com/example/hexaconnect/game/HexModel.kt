package com.example.hexaconnect.game

const val BOARD_SIZE = 9

enum class Player {
    PLAYER_ONE,
    PLAYER_TWO;

    val opponent: Player
        get() = if (this == PLAYER_ONE) PLAYER_TWO else PLAYER_ONE

    val number: Int
        get() = if (this == PLAYER_ONE) 1 else 2
}

data class HexCell(
    val row: Int,
    val col: Int,
)

enum class CellState {
    EMPTY,
    PLAYER_1,
    PLAYER_2,
    WINNING_PATH,
}

data class HexaConnectState(
    val board: List<List<Player?>>,
    val currentPlayer: Player = Player.PLAYER_ONE,
    val lastMove: HexCell? = null,
    val winningPath: List<HexCell> = emptyList(),
    val winner: Player? = null,
    val moveHistory: List<HexCell> = emptyList(),
    val isAnimating: Boolean = false,
) {
    val size: Int get() = board.size

    val isGameOver: Boolean get() = winner != null

    val canUndo: Boolean get() = moveHistory.isNotEmpty() && !isAnimating && winner == null

    fun ownerOf(cell: HexCell): Player? = board[cell.row][cell.col]

    fun cellState(cell: HexCell): CellState = when {
        cell in winningPath -> CellState.WINNING_PATH
        else -> when (ownerOf(cell)) {
            Player.PLAYER_ONE -> CellState.PLAYER_1
            Player.PLAYER_TWO -> CellState.PLAYER_2
            null -> CellState.EMPTY
        }
    }

    companion object {
        fun initial(size: Int = BOARD_SIZE): HexaConnectState =
            HexaConnectState(board = List(size) { List(size) { null } })
    }
}
