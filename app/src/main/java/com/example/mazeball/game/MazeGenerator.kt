package com.example.mazeball.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.example.mazeball.shared.ApiLevel
import kotlin.math.max
import kotlin.math.min

object MazeGenerator {

    private const val WALL_THICKNESS = 20f

    fun generate(apiLevel: ApiLevel, mazeSizePx: Float): RenderableMaze {
        val r = WALL_THICKNESS / 2
        val gameAreaSize = mazeSizePx - WALL_THICKNESS
        val offset = r

        val cellSize = gameAreaSize / apiLevel.gridSize

        fun toPx(coord: Int) = (coord * cellSize) + offset

        val walls = apiLevel.walls.map { wallDef ->
            val startX = toPx(wallDef.startX)
            val startY = toPx(wallDef.startY)
            val endX = toPx(wallDef.endX)
            val endY = toPx(wallDef.endY)

            if (wallDef.startX == wallDef.endX) {
                Rect(
                    left = startX - r,
                    top = min(startY, endY) - r,
                    right = startX + r,
                    bottom = max(startY, endY) + r
                )
            } else {
                Rect(
                    left = min(startX, endX) - r,
                    top = startY - r,
                    right = max(startX, endX) + r,
                    bottom = startY + r
                )
            }
        }

        val startOffset = Offset(
            x = toPx(apiLevel.start.x) + cellSize / 2,
            y = toPx(apiLevel.start.y) + cellSize / 2
        )

        val exitRect = Rect(
            left = toPx(apiLevel.exit.x),
            top = toPx(apiLevel.exit.y),
            right = toPx(apiLevel.exit.x) + cellSize,
            bottom = toPx(apiLevel.exit.y) + cellSize
        )

        return RenderableMaze(
            walls = walls,
            wallDefs = apiLevel.walls,
            gridSize = apiLevel.gridSize,
            start = startOffset,
            exit = exitRect
        )
    }
}
