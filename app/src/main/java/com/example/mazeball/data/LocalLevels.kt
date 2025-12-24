package com.example.mazeball.data

import com.example.mazeball.shared.ApiLevel
import com.example.mazeball.shared.GridPoint
import com.example.mazeball.shared.WallDefinition

object LocalLevels {
    val level1 = ApiLevel(
        id = 1,
        name = "Уровень 1",
        gridSize = 5,
        start = GridPoint(4, 0),
        exit = GridPoint(2, 5),
        walls = listOf(
            WallDefinition(0, 0, 5, 0), WallDefinition(5, 0, 5, 5), WallDefinition(0, 0, 0, 5),
            WallDefinition(0, 5, 2, 5), WallDefinition(3, 5, 5, 5), WallDefinition(4, 0, 4, 1),
            WallDefinition(4, 2, 5, 2), WallDefinition(3, 3, 4, 3), WallDefinition(4, 4, 5, 4),
            WallDefinition(1, 1, 3, 1), WallDefinition(1, 4, 3, 4), WallDefinition(2, 2, 3, 2),
            WallDefinition(1, 1, 1, 4), WallDefinition(3, 1, 3, 2), WallDefinition(2, 2, 2, 3),
            WallDefinition(3, 3, 3, 5)
        )
    )
    val level2 = ApiLevel(
        id = 2,
        name = "Уровень 2",
        gridSize = 7,
        start = GridPoint(2, 3),
        exit = GridPoint(3, 7),
        walls = listOf(
            WallDefinition(0, 0, 7, 0), WallDefinition(0, 0, 0, 7),
            WallDefinition(7, 0, 7, 7), WallDefinition(0, 7, 3, 7),
            WallDefinition(4, 7, 7, 7), WallDefinition(0, 1, 1, 1),
            WallDefinition(2, 1, 4, 1), WallDefinition(5, 1, 6, 1),
            WallDefinition(1, 2, 3, 2), WallDefinition(0, 3, 1, 3),
            WallDefinition(3, 3, 6, 3), WallDefinition(3, 4, 4, 4),
            WallDefinition(4, 5, 5, 5), WallDefinition(2, 1, 2, 6),
            WallDefinition(4, 1, 4, 4), WallDefinition(5, 1, 5, 2),
            WallDefinition(6, 1, 6, 6), WallDefinition(1, 3, 1, 6),
            WallDefinition(3, 4, 3, 7), WallDefinition(4, 5, 4, 7),
            WallDefinition(5, 4, 5, 5), WallDefinition(5, 6, 5, 7)
        )
    )
    val level3 = ApiLevel(
        id = 3,
        name = "Уровень 3",
        gridSize = 10,
        start = GridPoint(8, 3),
        exit = GridPoint(5, 10),
        walls = listOf(
            WallDefinition(0, 0, 0, 10), WallDefinition(10, 0, 10, 10),
            WallDefinition(0, 0, 10, 0),
            WallDefinition(0, 10, 5, 10), WallDefinition(6, 10, 10, 10),
            WallDefinition(0, 1, 1, 1), WallDefinition(3, 1, 5, 1),
            WallDefinition(8, 1, 9, 1), WallDefinition(2, 2, 3, 2),
            WallDefinition(5, 2 ,6, 2), WallDefinition(7, 2, 9, 2),
            WallDefinition(4, 3, 6, 3), WallDefinition(8, 3, 10, 3),
            WallDefinition(1, 4, 5, 4), WallDefinition(7, 4, 8, 4),
            WallDefinition(0, 5, 4, 5), WallDefinition(5, 5, 9, 5),
            WallDefinition(1, 6, 3, 6), WallDefinition(4, 6, 5, 6),
            WallDefinition(6, 6, 10, 6), WallDefinition(3, 7, 6, 7),
            WallDefinition(8, 7, 9, 7), WallDefinition(0, 8, 4, 8),
            WallDefinition(6, 8, 7, 8), WallDefinition(9, 8, 10, 8),
            WallDefinition(4, 9, 6, 9), WallDefinition(7, 9, 9, 9),
            WallDefinition(2, 0, 2, 3), WallDefinition(7, 0, 7, 1),
            WallDefinition(4, 1, 4, 3), WallDefinition(6, 1, 6, 3),
            WallDefinition(8, 1,8, 2), WallDefinition(1, 2, 1, 4),
            WallDefinition(3, 2,3, 4), WallDefinition(5, 3, 5, 4),
            WallDefinition(7, 2, 7, 4), WallDefinition(9, 3, 9, 4),
            WallDefinition(6, 4, 6, 5), WallDefinition(8, 4, 8, 5),
            WallDefinition(5, 5, 5, 6), WallDefinition(1, 6, 1, 7),
            WallDefinition(2, 6, 2, 9), WallDefinition(4, 6, 4, 7),
            WallDefinition(6, 6, 6, 7), WallDefinition(7, 7, 7, 8),
            WallDefinition(8, 7, 8, 9), WallDefinition(5, 8, 5, 10),
            WallDefinition(6, 8, 6, 9), WallDefinition(7, 9, 7, 10),
            WallDefinition(1, 9, 1, 10), WallDefinition(3, 9, 3, 10)
        )
    )

    val allGameLevels = listOf(level1, level2, level3)
}
