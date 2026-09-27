package com.example.hexaconnect.game

/**
 * Board topology for a rhombus of pointy-top hexagons.
 *
 * Coordinates are (row, col). Each row is drawn half a hex further right than the row above it,
 * so the board reads as a parallelogram. With that offset, the six neighbours of (r, c) are:
 *
 *   upper-left (r-1, c)    upper-right (r-1, c+1)
 *   left       (r, c-1)    right       (r, c+1)
 *   lower-left (r+1, c-1)  lower-right (r+1, c)
 *
 * This is the only place in the app that defines adjacency.
 */
object HexGrid {

    private val NEIGHBOR_OFFSETS = listOf(
        -1 to 0,
        -1 to 1,
        0 to -1,
        0 to 1,
        1 to -1,
        1 to 0,
    )

    fun isInside(cell: HexCell, size: Int = BOARD_SIZE): Boolean =
        cell.row in 0 until size && cell.col in 0 until size

    fun getNeighbors(cell: HexCell, size: Int = BOARD_SIZE): List<HexCell> =
        NEIGHBOR_OFFSETS
            .map { (dr, dc) -> HexCell(cell.row + dr, cell.col + dc) }
            .filter { isInside(it, size) }

    fun allCells(size: Int = BOARD_SIZE): List<HexCell> =
        (0 until size).flatMap { r -> (0 until size).map { c -> HexCell(r, c) } }
}
