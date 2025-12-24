package com.example.mazeball.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mazeball.R
import com.example.mazeball.data.LevelRepository
import com.example.mazeball.shared.ApiLevel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Locale

@Composable
fun GameScreen(
    levels: List<ApiLevel>,
    currentLevelIndex: Int,
    navController: NavController,
    levelRepository: LevelRepository
) {
    val level = levels[currentLevelIndex]
    val context = LocalContext.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val mazeSizePx = with(LocalDensity.current) { minOf(maxWidth, maxHeight).toPx() }
        val maze = remember(level, mazeSizePx) { MazeGenerator.generate(level, mazeSizePx) }
        val gameEngine = remember(maze) { GameEngine(context, maze) }
        val gameState = gameEngine.gameState
        val coroutineScope = rememberCoroutineScope()

        DisposableEffect(gameEngine) {
            onDispose { gameEngine.release() }
        }

        GameCanvas(maze, gameState, mazeSizePx)

        if (gameState.gameWon) {
            var isNewBestTime by remember { mutableStateOf(false) }
            var existingBestTime by remember { mutableStateOf<Long?>(null) }

            LaunchedEffect(Unit) {
                 coroutineScope.launch {
                    existingBestTime = levelRepository.userPreferencesRepository.getBestTime(level.id)
                    isNewBestTime = levelRepository.userPreferencesRepository.updateBestTime(level.id, gameState.elapsedTime)
                 }
            }

            fun navigateBack() {
                if (isNewBestTime) {
                    navController.previousBackStackEntry?.savedStateHandle?.set("new_record_set", true)
                }
                navController.popBackStack()
            }

            BackHandler { navigateBack() }

            VictoryDialog(
                elapsedTime = gameState.elapsedTime,
                isNewBestTime = isNewBestTime,
                existingBestTime = existingBestTime,
                onGoToLevelSelection = { navigateBack() },
                onReplay = { gameEngine.resetGame() },
                onPrevLevel = {
                    val prevIndex = currentLevelIndex - 1
                    if (prevIndex >= 0) {
                        val levelsJson = Json.encodeToString(levels)
                        navController.navigate("game/$levelsJson/$prevIndex") { 
                            popUpTo("game/{levelsJson}/{currentLevelIndex}") { inclusive = true }
                        }
                    }
                },
                onNextLevel = {
                    val nextIndex = currentLevelIndex + 1
                    if (nextIndex < levels.size) {
                        val levelsJson = Json.encodeToString(levels)
                        navController.navigate("game/$levelsJson/$nextIndex") {
                            popUpTo("game/{levelsJson}/{currentLevelIndex}") { inclusive = true }
                        }
                    }
                },
                hasPrevLevel = currentLevelIndex > 0,
                hasNextLevel = currentLevelIndex < levels.size - 1
            )
        }
    }
}

@Composable
private fun GameCanvas(maze: RenderableMaze, gameState: GameState, mazeSizePx: Float) {
    val wallColor = Color(0xFFFF4917)
    val backgroundColor = Color(0xFF002245)

    Box(
        modifier = Modifier.fillMaxSize().background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (!gameState.gameWon) {
            Text(
                text = String.format(Locale.US, "Время: %.3f", gameState.elapsedTime / 1000.0),
                style = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold),
                color = Color(0xFFF9C846),
                modifier = Modifier.align(Alignment.TopCenter).padding(32.dp)
            )
        }

        Box(modifier = Modifier.size(with(LocalDensity.current) { mazeSizePx.toDp() })) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val wallThickness = 20f
                val cellSize = size.width / maze.gridSize
                fun toPx(coord: Int) = coord * cellSize

                maze.wallDefs.forEach { wallDef ->
                    drawLine(
                        color = wallColor,
                        start = Offset(toPx(wallDef.startX), toPx(wallDef.startY)),
                        end = Offset(toPx(wallDef.endX), toPx(wallDef.endY)),
                        strokeWidth = wallThickness,
                        cap = StrokeCap.Round
                    )
                }
            }
            Ball(position = gameState.ballPosition, scrollOffset = gameState.scrollOffset)
        }
    }
}

@Composable
private fun VictoryDialog(
    elapsedTime: Long,
    isNewBestTime: Boolean,
    existingBestTime: Long?,
    onGoToLevelSelection: () -> Unit,
    onReplay: () -> Unit,
    onPrevLevel: () -> Unit,
    onNextLevel: () -> Unit,
    hasPrevLevel: Boolean,
    hasNextLevel: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.widthIn(max = 400.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF002245).copy(alpha = 0.9f))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.trophy),
                    contentDescription = "Победа!",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (existingBestTime != null && !isNewBestTime) {
                    Text("Лучшее время: ${String.format(Locale.US, "%.3f", existingBestTime / 1000.0)}", color = Color.Gray, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val resultText = if (isNewBestTime) "Новый рекорд!" else "Ваше время:"
                Text(
                    text = "$resultText ${String.format(Locale.US, "%.3f", elapsedTime / 1000.0)}",
                    fontSize = 28.sp,
                    color = if (isNewBestTime) Color.Yellow else Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevLevel, enabled = hasPrevLevel, modifier = Modifier.padding(horizontal = 8.dp)) {
                        Icon(Icons.Default.ArrowBack, "Previous Level", tint = if(hasPrevLevel) Color.White else Color.Gray, modifier = Modifier.size(36.dp))
                    }
                    IconButton(onClick = onGoToLevelSelection, modifier = Modifier.padding(horizontal = 8.dp)) {
                        Icon(Icons.Default.List, "All Levels", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    IconButton(onClick = onReplay, modifier = Modifier.padding(horizontal = 8.dp)) {
                        Icon(Icons.Default.Refresh, "Replay", tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                    IconButton(onClick = onNextLevel, enabled = hasNextLevel, modifier = Modifier.padding(horizontal = 8.dp)) {
                        Icon(Icons.Default.ArrowForward, "Next Level", tint = if(hasNextLevel) Color.White else Color.Gray, modifier = Modifier.size(36.dp))
                    }
                }
            }
        }
    }
}
