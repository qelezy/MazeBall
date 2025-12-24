package com.example.mazeball

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mazeball.data.LevelRepository
import com.example.mazeball.data.UserPreferencesRepository
import com.example.mazeball.shared.ApiLevel
import com.example.mazeball.ui.LevelSelectionUiState
import com.example.mazeball.ui.LevelSelectionViewModel
import com.example.mazeball.ui.LevelSelectionViewModelFactory

@Composable
fun LevelSelectionScreen(
    navController: NavController, 
    levelRepository: LevelRepository,
    userPreferencesRepository: UserPreferencesRepository,
    onLevelSelected: (List<ApiLevel>, Int) -> Unit,
    onBack: () -> Unit,
) {
    val vm: LevelSelectionViewModel = viewModel(factory = LevelSelectionViewModelFactory(levelRepository, userPreferencesRepository))
    val uiState = vm.uiState

    LaunchedEffect(Unit) {
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<Boolean>("new_record_set")?.observeForever { isNewRecord ->
            if (isNewRecord) {
                vm.loadLevelsAndBestTimes()
                savedStateHandle.set("new_record_set", false)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF002245)),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is LevelSelectionUiState.Loading -> CircularProgressIndicator()
            is LevelSelectionUiState.Error -> Text(text = uiState.message, color = Color.Red)
            is LevelSelectionUiState.Success -> {
                val bestTimes by vm.bestTimes.collectAsState()
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 64.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(uiState.levels) { index, level ->
                        LevelCard(level = level, bestTime = bestTimes[level.id], onClick = {
                            onLevelSelected(uiState.levels, index)
                        })
                    }
                }
            }
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
        }
    }
}

@Composable
fun LevelCard(level: ApiLevel, bestTime: Long?, onClick: () -> Unit) {
    Card(
        modifier = Modifier.size(150.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF048A81)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = level.name,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Best Time",
                    tint = if (bestTime != null) Color.Yellow else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (bestTime != null) "%.3f".format(bestTime / 1000.0) else "--:--",
                    color = if (bestTime != null) Color.Yellow else Color.Gray,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
