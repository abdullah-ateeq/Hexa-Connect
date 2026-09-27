package com.example.hexaconnect.ui.board

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import com.example.hexaconnect.game.HexCell
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

private val SQRT3 = sqrt(3f)

/**
 * Pixel layout of an n×n rhombus of pointy-top hexagons fitted into [canvas].
 *
 * In normalized units (hex radius = 1) the centre of (row, col) is
 * x = √3·(col + row/2) + √3/2, y = 1.5·row + 1; everything is then scaled by [radius] and centred.
 */
class HexGeometry(val n: Int, canvas: Size, paddingFactor: Float = 0.5f) {

    val radius: Float
    private val origin: Offset

    init {
        val unitsWide = SQRT3 * (n + (n - 1) / 2f) + 2 * paddingFactor
        val unitsHigh = 1.5f * (n - 1) + 2f + 2 * paddingFactor
        radius = min(canvas.width / unitsWide, canvas.height / unitsHigh).coerceAtLeast(0f)
        val boardW = SQRT3 * (n + (n - 1) / 2f) * radius
        val boardH = (1.5f * (n - 1) + 2f) * radius
        origin = Offset((canvas.width - boardW) / 2f, (canvas.height - boardH) / 2f)
    }

    val hexWidth: Float get() = SQRT3 * radius

    fun center(row: Int, col: Int): Offset = Offset(
        origin.x + hexWidth * (col + row / 2f) + hexWidth / 2f,
        origin.y + radius + 1.5f * radius * row,
    )

    fun center(cell: HexCell): Offset = center(cell.row, cell.col)

    /** Corner i of a pointy-top hex: 0 upper-right, 1 lower-right, 2 bottom, 3 lower-left, 4 upper-left, 5 top. */
    fun corner(center: Offset, i: Int, r: Float = radius): Offset {
        val angle = (PI / 180.0 * (60 * i - 30)).toFloat()
        return Offset(center.x + r * cos(angle), center.y + r * sin(angle))
    }

    fun hexPath(center: Offset, r: Float): Path = Path().apply {
        moveTo(center.x + r * cos(-PI.toFloat() / 6), center.y + r * sin(-PI.toFloat() / 6))
        for (i in 1..5) {
            val p = corner(center, i, r)
            lineTo(p.x, p.y)
        }
        close()
    }

    /**
     * Finds the hex under [point] using nearest-centre distance, which matches the true hexagon
     * shape inside the board. Taps slightly outside the outer edge still resolve (forgiving input).
     */
    fun hitTest(point: Offset): HexCell? {
        var best: HexCell? = null
        var bestDist = Float.MAX_VALUE
        for (r in 0 until n) for (c in 0 until n) {
            val d = (center(r, c) - point).getDistanceSquared()
            if (d < bestDist) {
                bestDist = d
                best = HexCell(r, c)
            }
        }
        val limit = radius * 1.1f
        return if (bestDist <= limit * limit) best else null
    }

    // Rails trace the outer edges of each side's hexes.
    fun topRail(): List<Offset> =
        (0 until n).flatMap { c -> listOf(corner(center(0, c), 4), corner(center(0, c), 5)) } +
            corner(center(0, n - 1), 0)

    fun bottomRail(): List<Offset> =
        (0 until n).flatMap { c -> listOf(corner(center(n - 1, c), 3), corner(center(n - 1, c), 2)) } +
            corner(center(n - 1, n - 1), 1)

    fun leftRail(): List<Offset> =
        (0 until n).flatMap { r -> listOf(corner(center(r, 0), 4), corner(center(r, 0), 3)) }

    fun rightRail(): List<Offset> =
        (0 until n).flatMap { r -> listOf(corner(center(r, n - 1), 0), corner(center(r, n - 1), 1)) }

    /**
     * Convex outline of the whole board. Filling it and stroking it with width 2·m (round joins)
     * gives a backing plate that sits m pixels outside every cell.
     */
    fun boardOutline(): Path {
        val last = n - 1
        val points = listOf(
            corner(center(0, 0), 4),
            corner(center(0, 0), 5),
            corner(center(0, last), 5),
            corner(center(0, last), 0),
            corner(center(last, last), 0),
            corner(center(last, last), 1),
            corner(center(last, last), 2),
            corner(center(last, 0), 2),
            corner(center(last, 0), 3),
            corner(center(0, 0), 3),
        )
        return Path().apply {
            moveTo(points[0].x, points[0].y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
            close()
        }
    }
}
