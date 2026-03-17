package com.hope_finder.ui.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hope_finder.data.model.RadarCell
import com.hope_finder.ui.theme.CmdAlert
import com.hope_finder.ui.theme.CmdActive
import com.hope_finder.ui.theme.CmdBackground
import com.hope_finder.ui.theme.CmdOffline
import com.hope_finder.ui.theme.CmdPrimary
import com.hope_finder.ui.theme.CmdSurface
import com.hope_finder.ui.theme.CmdWarning
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// ── Hex Cell State ────────────────────────────────────────────────────────────

enum class HexCellState {
    ACTIVE,          // probe active  → green
    LIFE_DETECTED,   // life signal   → red
    OFFLINE,         // probe offline → gray
    WEAK_SIGNAL;     // weak          → yellow

    val color: Color get() = when (this) {
        ACTIVE        -> CmdActive
        LIFE_DETECTED -> CmdAlert
        OFFLINE       -> CmdOffline
        WEAK_SIGNAL   -> CmdWarning
    }

    val glowColor: Color get() = color.copy(alpha = 0.25f)
}

// ── Data ──────────────────────────────────────────────────────────────────────

data class HexCellData(
    val id: String,
    val col: Int,
    val row: Int,
    val state: HexCellState,
    val label: String = ""
)

fun RadarCell.toHexCellState(): HexCellState = when (status.lowercase()) {
    "active", "online", "scanning" -> HexCellState.ACTIVE
    "life detected"                 -> HexCellState.LIFE_DETECTED
    "weak", "weak signal"           -> HexCellState.WEAK_SIGNAL
    else                            -> HexCellState.OFFLINE
}

// ── Geometry helpers ──────────────────────────────────────────────────────────

/**
 * Returns the 6 vertices of a flat-top hexagon centered at [center] with
 * the given outer radius [r].
 */
private fun hexVertices(center: Offset, r: Float): List<Offset> =
    (0..5).map { i ->
        val angle = Math.toRadians((60.0 * i) - 30.0).toFloat()
        Offset(center.x + r * cos(angle), center.y + r * sin(angle))
    }

/**
 * Builds a closed [Path] for a flat-top regular hexagon.
 */
private fun hexPath(center: Offset, r: Float): Path {
    val verts = hexVertices(center, r)
    return Path().apply {
        moveTo(verts[0].x, verts[0].y)
        for (i in 1..5) lineTo(verts[i].x, verts[i].y)
        close()
    }
}

/**
 * Pixel-center of hex cell at grid (col, row) using offset coordinates.
 * Uses even-column offset: odd columns shift down by half a hex height.
 */
fun hexCenter(col: Int, row: Int, hexSize: Float): Offset {
    val hexWidth  = sqrt(3f) * hexSize          // pointy-top width
    val hexHeight = 2f * hexSize                 // flat-top height
    val xOffset   = col * (hexWidth)
    val yOffset   = row * (hexHeight * 0.75f) + if (col % 2 == 1) hexHeight * 0.375f else 0f
    return Offset(xOffset, yOffset)
}

// ── DrawScope extensions ──────────────────────────────────────────────────────

private fun DrawScope.drawHexCell(
    cell: HexCellData,
    center: Offset,
    hexSize: Float,
    glowAlpha: Float,
    isSelected: Boolean
) {
    val fillColor  = cell.state.color
    val glowColor  = cell.state.glowColor
    val path       = hexPath(center, hexSize * 0.92f)   // slight inset for gap
    val glowPath   = hexPath(center, hexSize * 1.05f)

    // glow halo (animated alpha)
    if (cell.state != HexCellState.OFFLINE) {
        drawPath(glowPath, color = glowColor.copy(alpha = glowAlpha * 0.5f))
        drawPath(glowPath, color = glowColor.copy(alpha = 0f),
            style = Stroke(width = hexSize * 0.12f))
    }

    // fill
    drawPath(path, color = fillColor.copy(alpha = if (cell.state == HexCellState.OFFLINE) 0.3f else 0.18f))

    // border
    val borderColor = if (isSelected) Color.White else fillColor
    drawPath(path, color = borderColor.copy(alpha = if (isSelected) 1f else 0.75f),
        style = Stroke(width = if (isSelected) 2.5f else 1.2f, cap = StrokeCap.Round))

    // selected glow ring
    if (isSelected) {
        drawPath(hexPath(center, hexSize * 1.1f),
            color = Color.White.copy(alpha = 0.25f),
            style = Stroke(width = 1f))
    }
}

// ── Public composable ─────────────────────────────────────────────────────────

/**
 * Draws a honeycomb-style hexagonal radar grid.
 *
 * @param cells        Live cell data (from Firestore via HomeViewModel).
 * @param cols         Number of hex columns.
 * @param rows         Number of hex rows.
 * @param onCellClick  Called with the [HexCellData] when a cell is tapped.
 */
@Composable
fun HexGridRadarView(
    cells: List<HexCellData>,
    cols: Int = 7,
    rows: Int = 5,
    onCellClick: (HexCellData) -> Unit = {}
) {
    var selectedId by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "hex_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Scan-sweep angle
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(
            animation  = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CmdSurface)
            .padding(12.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(cols.toFloat() / (rows * 0.85f))
                .pointerInput(cells) {
                    detectTapGestures { tapOffset ->
                        val canvasW = size.width.toFloat()
                        val canvasH = size.height.toFloat()
                        val hexSize = computeHexSize(canvasW, canvasH, cols, rows)
                        val (ox, oy) = gridOrigin(canvasW, canvasH, cols, rows, hexSize)

                        cells.forEach { cell ->
                            val c = hexCenter(cell.col, cell.row, hexSize)
                                .translate(ox, oy)
                            if (tapOffset.distanceTo(c) < hexSize * 0.9f) {
                                selectedId = if (selectedId == cell.id) null else cell.id
                                onCellClick(cell)
                            }
                        }
                    }
                }
        ) {
            val hexSize = computeHexSize(size.width, size.height, cols, rows)
            val (ox, oy) = gridOrigin(size.width, size.height, cols, rows, hexSize)

            // ── background grid lines (dim) ───────────────────────────────
            for (col in 0 until cols) {
                for (row in 0 until rows) {
                    val c = hexCenter(col, row, hexSize).translate(ox, oy)
                    val path = hexPath(c, hexSize * 0.92f)
                    drawPath(path, color = CmdBackground.copy(alpha = 0.6f))
                    drawPath(path, color = CmdPrimary.copy(alpha = 0.08f),
                        style = Stroke(width = 0.8f))
                }
            }

            // ── live cells ────────────────────────────────────────────────
            cells.forEach { cell ->
                if (cell.col in 0 until cols && cell.row in 0 until rows) {
                    val c = hexCenter(cell.col, cell.row, hexSize).translate(ox, oy)
                    drawHexCell(cell, c, hexSize, glowAlpha, selectedId == cell.id)
                }
            }

            // ── radar sweep ───────────────────────────────────────────────
            val cx = size.width / 2f
            val cy = size.height / 2f
            val sweepR = minOf(size.width, size.height) * 0.48f
            val sweepRad = Math.toRadians(sweepAngle.toDouble()).toFloat()
            drawLine(
                color = CmdPrimary.copy(alpha = 0.35f),
                start  = Offset(cx, cy),
                end    = Offset(cx + sweepR * cos(sweepRad), cy + sweepR * sin(sweepRad)),
                strokeWidth = 1.5f,
                cap    = StrokeCap.Round
            )
            // subtle arc trail
            for (i in 1..8) {
                val trailAngle = sweepAngle - i * 4f
                val tRad = Math.toRadians(trailAngle.toDouble()).toFloat()
                val tLen = sweepR * (1f - i * 0.1f)
                drawLine(
                    color = CmdPrimary.copy(alpha = (0.3f - i * 0.035f).coerceAtLeast(0f)),
                    start  = Offset(cx, cy),
                    end    = Offset(cx + tLen * cos(tRad), cy + tLen * sin(tRad)),
                    strokeWidth = 1f
                )
            }
        }
    }
}

// ── Utility ───────────────────────────────────────────────────────────────────

private fun computeHexSize(canvasW: Float, canvasH: Float, cols: Int, rows: Int): Float {
    val hexW = canvasW / (cols * sqrt(3f) + 0.5f * sqrt(3f))
    val hexH = canvasH / (rows * 1.5f + 0.5f)
    return minOf(hexW, hexH) * 0.92f
}

private fun gridOrigin(
    canvasW: Float,
    canvasH: Float,
    cols: Int,
    rows: Int,
    hexSize: Float
): Pair<Float, Float> {
    val hexWidth  = sqrt(3f) * hexSize
    val hexHeight = 2f * hexSize
    val totalW    = cols * hexWidth
    val totalH    = rows * hexHeight * 0.75f + hexHeight * 0.25f
    return Pair((canvasW - totalW) / 2f + hexWidth / 2f,
                (canvasH - totalH) / 2f + hexHeight / 2f)
}

private fun Offset.translate(dx: Float, dy: Float) = Offset(x + dx, y + dy)
private fun Offset.distanceTo(other: Offset): Float {
    val dx = x - other.x
    val dy = y - other.y
    return sqrt(dx * dx + dy * dy)
}

// ── Preview helpers ───────────────────────────────────────────────────────────

fun sampleHexCells(cols: Int = 7, rows: Int = 5): List<HexCellData> {
    val states = listOf(
        HexCellState.ACTIVE,
        HexCellState.OFFLINE,
        HexCellState.LIFE_DETECTED,
        HexCellState.WEAK_SIGNAL,
        HexCellState.ACTIVE,
        HexCellState.ACTIVE,
        HexCellState.OFFLINE
    )
    return (0 until cols).flatMap { col ->
        (0 until rows).map { row ->
            HexCellData(
                id    = "probe_${col}_${row}",
                col   = col,
                row   = row,
                state = states[(col + row * 2) % states.size],
                label = "R${row}C${col}"
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0F1A)
@Composable
private fun HexGridPreview() {
    HexGridRadarView(cells = sampleHexCells())
}
