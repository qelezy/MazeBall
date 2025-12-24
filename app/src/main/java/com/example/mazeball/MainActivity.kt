package com.example.mazeball

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mazeball.data.LevelRepository
import com.example.mazeball.data.UserPreferencesRepository
import com.example.mazeball.game.GameScreen
import com.example.mazeball.shared.ApiLevel
import com.example.mazeball.ui.theme.MazeBallTheme
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MazeBallTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    val userPreferencesRepository = UserPreferencesRepository(applicationContext)
                    val levelRepository = LevelRepository(userPreferencesRepository)

                    NavHost(
                        navController = navController,
                        startDestination = "mainMenu",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("mainMenu") {
                            MainMenuScreen(
                                navController = navController,
                                userPreferencesRepository = userPreferencesRepository
                            )
                        }
                        composable("level_selection") {
                            LevelSelectionScreen(
                                navController = navController,
                                levelRepository = levelRepository,
                                userPreferencesRepository = userPreferencesRepository,
                                onLevelSelected = { levels, index ->
                                    val levelsJson = URLEncoder.encode(Json.encodeToString(levels), StandardCharsets.UTF_8.toString())
                                    navController.navigate("game/$levelsJson/$index")
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "game/{levelsJson}/{currentLevelIndex}",
                            arguments = listOf(
                                navArgument("levelsJson") { type = NavType.StringType },
                                navArgument("currentLevelIndex") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val levelsJson = backStackEntry.arguments?.getString("levelsJson")?.let {
                                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                            }
                            val currentLevelIndex = backStackEntry.arguments?.getInt("currentLevelIndex")
                            val levels = levelsJson?.let { Json.decodeFromString<List<ApiLevel>>(it) }
                            if (levels != null && currentLevelIndex != null) {
                                GameScreen(
                                    levels = levels,
                                    currentLevelIndex = currentLevelIndex,
                                    navController = navController,
                                    levelRepository = levelRepository
                                )
                            }
                        }
                        composable("leaderboard") {
                            LeaderboardScreen(
                                levelRepository = levelRepository,
                                userPreferencesRepository = userPreferencesRepository,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
