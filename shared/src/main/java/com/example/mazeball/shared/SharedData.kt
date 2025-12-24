package com.example.mazeball.shared

import kotlinx.serialization.Serializable

/**
 * Абстрактное описание уровня, которое легко сериализуется в/из JSON.
 */
@Serializable
data class ApiLevel(
    val id: Int,
    val name: String,
    val gridSize: Int,
    val start: GridPoint,
    val exit: GridPoint,
    val walls: List<WallDefinition>
)

/**
 * Описание одной стены в сеточных координатах.
 */
@Serializable
data class WallDefinition(val startX: Int, val startY: Int, val endX: Int, val endY: Int)

/**
 * Простая структура для хранения координат в сетке.
 */
@Serializable
data class GridPoint(val x: Int, val y: Int)

/**
 * Запись в таблице лидеров.
 */
@Serializable
data class LeaderboardEntry(val deviceId: String, var playerName: String, var timeMillis: Long)


@Serializable
data class SubmitRequest(val levelId: Int, val deviceId: String, val timeMillis: Long)

@Serializable
data class UpdateNicknameRequest(val deviceId: String, val newNickname: String)

@Serializable
data class SyncRequest(val deviceId: String, val scores: List<SubmitRequest>)
