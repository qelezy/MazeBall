package com.example.mazeball.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlinx.serialization.Serializable

@Serializable
data class ApiLevel(
    val id: Int,
    val name: String,
    val gridSize: Int,
    val start: GridPoint,
    val exit: GridPoint,
    val walls: List<WallDefinition>
)

@Serializable
data class WallDefinition(val startX: Int, val startY: Int, val endX: Int, val endY: Int)

@Serializable
data class GridPoint(val x: Int, val y: Int)

data class RenderableMaze(
    val walls: List<Rect>,
    val wallDefs: List<com.example.mazeball.shared.WallDefinition>,
    val gridSize: Int,
    val start: Offset,
    val exit: Rect
)
