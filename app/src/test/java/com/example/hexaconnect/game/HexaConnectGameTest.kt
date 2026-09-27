package com.example.hexaconnect.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HexaConnectGameTest {

    /** Places each move in order, asserting it is accepted. */
    private fun HexaConnectGame.play(vararg moves: HexCell) {
        moves.forEach { assertTrue("move $it rejected", placePiece(it)) }
    }

    @Test
    fun initialBoardIsEmptyAndPlayerOneStarts() {
        val state = HexaConnectGame().state
        assertEquals(9, state.board.size)
        assertTrue(state.board.all { it.size == 9 && it.all { owner -> owner == null } })
        assertEquals(Player.PLAYER_ONE, state.currentPlayer)
        assertNull(state.winner)
        assertTrue(state.moveHistory.isEmpty())
    }

    @Test
    fun legalPlacementClaimsCellAndSwitchesPlayer() {
        val game = HexaConnectGame()
        assertTrue(game.placePiece(HexCell(4, 4)))
        assertEquals(Player.PLAYER_ONE, game.state.ownerOf(HexCell(4, 4)))
        assertEquals(HexCell(4, 4), game.state.lastMove)
        assertEquals(Player.PLAYER_TWO, game.state.currentPlayer)

        assertTrue(game.placePiece(HexCell(0, 0)))
        assertEquals(Player.PLAYER_TWO, game.state.ownerOf(HexCell(0, 0)))
        assertEquals(Player.PLAYER_ONE, game.state.currentPlayer)
    }

    @Test
    fun occupiedOutOfBoardAndLockedMovesAreRejected() {
        val game = HexaConnectGame()
        game.placePiece(HexCell(2, 2))
        val before = game.state
        assertFalse(game.placePiece(HexCell(2, 2)))
        assertFalse(game.placePiece(HexCell(-1, 3)))
        assertFalse(game.placePiece(HexCell(3, 9)))
        assertEquals(before, game.state)

        game.setAnimating(true)
        assertFalse(game.placePiece(HexCell(5, 5)))
        assertFalse(game.undo())
    }

    @Test
    fun interiorCellHasExactlySixHexNeighbors() {
        val neighbors = HexGrid.getNeighbors(HexCell(4, 4)).toSet()
        assertEquals(
            setOf(
                HexCell(3, 4), HexCell(3, 5),
                HexCell(4, 3), HexCell(4, 5),
                HexCell(5, 3), HexCell(5, 4),
            ),
            neighbors,
        )
        // Diagonals of a square grid that are not hex neighbours.
        assertFalse(HexCell(3, 3) in neighbors)
        assertFalse(HexCell(5, 5) in neighbors)
    }

    @Test
    fun cornerAndEdgeNeighborsAreFiltered() {
        assertEquals(setOf(HexCell(0, 1), HexCell(1, 0)), HexGrid.getNeighbors(HexCell(0, 0)).toSet())
        assertEquals(
            setOf(HexCell(0, 7), HexCell(1, 7), HexCell(1, 8)),
            HexGrid.getNeighbors(HexCell(0, 8)).toSet(),
        )
        assertEquals(4, HexGrid.getNeighbors(HexCell(0, 4)).size)
    }

    @Test
    fun neighborRelationIsSymmetric() {
        for (cell in HexGrid.allCells()) {
            for (n in HexGrid.getNeighbors(cell)) {
                assertTrue("$cell <-> $n", cell in HexGrid.getNeighbors(n))
            }
        }
    }

    @Test
    fun playerOneWinsWithStraightTopToBottomColumn() {
        val game = HexaConnectGame()
        for (r in 0 until 9) {
            game.placePiece(HexCell(r, 0))
            if (r < 8) game.placePiece(HexCell(r, 8))
        }
        assertEquals(Player.PLAYER_ONE, game.state.winner)
        assertEquals((0 until 9).map { HexCell(it, 0) }, game.state.winningPath)
        assertEquals(Player.PLAYER_ONE, game.state.currentPlayer)
    }

    @Test
    fun playerTwoWinsWithLeftToRightRow() {
        val game = HexaConnectGame()
        for (c in 0 until 9) {
            game.placePiece(HexCell(8, c))
            game.placePiece(HexCell(4, c))
        }
        assertEquals(Player.PLAYER_TWO, game.state.winner)
        assertEquals((0 until 9).map { HexCell(4, it) }, game.state.winningPath)
    }

    @Test
    fun bentPathWinsAndIsReconstructedInOrder() {
        // Player 1 zig-zags down, runs sideways along row 3, then drops down the left column.
        val p1 = listOf(
            HexCell(0, 5), HexCell(1, 4), HexCell(2, 4), HexCell(3, 3),
            HexCell(3, 2), HexCell(3, 1), HexCell(4, 0), HexCell(5, 0),
            HexCell(6, 0), HexCell(7, 0), HexCell(8, 0),
        )
        val p2Filler = listOf(
            HexCell(0, 0), HexCell(0, 1), HexCell(0, 2), HexCell(0, 3), HexCell(0, 8),
            HexCell(1, 8), HexCell(2, 8), HexCell(3, 8), HexCell(4, 8), HexCell(5, 8),
        )
        val game = HexaConnectGame()
        p1.forEachIndexed { i, cell ->
            game.play(cell)
            if (i < p2Filler.size) game.play(p2Filler[i])
        }
        val state = game.state
        assertEquals(Player.PLAYER_ONE, state.winner)
        val path = state.winningPath
        assertEquals(0, path.first().row)
        assertEquals(8, path.last().row)
        path.zipWithNext().forEach { (a, b) -> assertTrue("$a-$b adjacent", b in HexGrid.getNeighbors(a)) }
        assertTrue(path.all { state.ownerOf(it) == Player.PLAYER_ONE })
        assertEquals(p1, path)
    }

    @Test
    fun disconnectedPiecesDoNotWin() {
        val game = HexaConnectGame()
        // Player 1 fills column 0 except row 4; Player 2 plugs the gap.
        val p1 = (0 until 9).filter { it != 4 }.map { HexCell(it, 0) } + HexCell(5, 5)
        val p2 = listOf(HexCell(4, 0)) + (0 until 8).map { HexCell(it, 8) }
        p1.zip(p2).forEach { (a, b) -> game.play(a, b) }
        assertNull(game.state.winner)
        assertNull(game.findWinningPath(Player.PLAYER_ONE))
        assertNull(game.findWinningPath(Player.PLAYER_TWO))
    }

    @Test
    fun placementAfterWinIsRejected() {
        val game = HexaConnectGame()
        for (r in 0 until 9) {
            game.placePiece(HexCell(r, 4))
            if (r < 8) game.placePiece(HexCell(r, 0))
        }
        val won = game.state
        assertFalse(game.placePiece(HexCell(8, 8)))
        assertEquals(won, game.state)
    }

    @Test
    fun undoRestoresCellPlayerAndWinState() {
        val game = HexaConnectGame()
        for (r in 0 until 9) {
            game.placePiece(HexCell(r, 4))
            if (r < 8) game.placePiece(HexCell(r, 0))
        }
        assertEquals(Player.PLAYER_ONE, game.state.winner)

        assertTrue(game.undo())
        assertNull(game.state.winner)
        assertTrue(game.state.winningPath.isEmpty())
        assertNull(game.state.ownerOf(HexCell(8, 4)))
        assertEquals(Player.PLAYER_ONE, game.state.currentPlayer)
        assertEquals(HexCell(7, 0), game.state.lastMove)

        assertTrue(game.undo())
        assertEquals(Player.PLAYER_TWO, game.state.currentPlayer)
        assertEquals(15, game.state.moveHistory.size)

        while (game.undo()) Unit
        assertEquals(HexaConnectState.initial(), game.state)
    }
}
