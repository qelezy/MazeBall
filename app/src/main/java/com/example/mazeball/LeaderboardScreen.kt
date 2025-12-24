package com.example.mazeball

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mazeball.data.LevelRepository
import com.example.mazeball.data.NicknameTakenException
import com.example.mazeball.data.UserPreferencesRepository
import com.example.mazeball.shared.ApiLevel
import com.example.mazeball.ui.LeaderboardUiState
import com.example.mazeball.ui.LeaderboardViewModel
import com.example.mazeball.ui.LeaderboardViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun LeaderboardScreen(
    levelRepository: LevelRepository, 
    userPreferencesRepository: UserPreferencesRepository, 
    onBack: () -> Unit
) {
    val vm: LeaderboardViewModel = viewModel(factory = LeaderboardViewModelFactory(levelRepository, userPreferencesRepository))
    val coroutineScope = rememberCoroutineScope()

    val uiState = vm.uiState
    val userPreferences by vm.userPreferences.collectAsState()
    var nicknameInput by remember(userPreferences.nickname) { mutableStateOf(userPreferences.nickname) }

    var isSyncing by remember { mutableStateOf(false) }
    var isSavingNickname by remember { mutableStateOf(false) }
    var nicknameError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF002245)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
                }
                Text("Списки лидеров", color = Color.White, fontSize = 20.sp)
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            isSyncing = true
                            try {
                                vm.syncScores()
                            } finally {
                                isSyncing = false
                            }
                        }
                    },
                    enabled = !isSyncing
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = "Синхронизировать", tint = Color.White)
                    }
                }
            }

            when (uiState) {
                is LeaderboardUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is LeaderboardUiState.Error -> {
                    Text(text = uiState.message, color = Color.Red, modifier = Modifier.padding(16.dp).weight(1f))
                }
                is LeaderboardUiState.Success -> {
                    var expandedLevelId by remember { mutableStateOf<Int?>(null) }
                    LazyColumn(modifier = Modifier.weight(1f).padding(16.dp)) {
                        items(uiState.levels) { level ->
                            LevelLeaderboardCard(
                                level = level,
                                isExpanded = expandedLevelId == level.id,
                                onExpand = { 
                                    expandedLevelId = if (expandedLevelId == level.id) null else level.id
                                },
                                leaderboard = uiState.leaderboards[level.id] ?: emptyList()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            if (uiState is LeaderboardUiState.Success) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (userPreferences.nickname.isBlank()) {
                        Text(
                            "Чтобы ваши рекорды попали в онлайн-рейтинг, введите никнейм.",
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = nicknameInput,
                        onValueChange = { 
                            nicknameInput = it
                            nicknameError = null
                        },
                        label = { Text("Ваш никнейм") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = nicknameError != null,
                        supportingText = { 
                            if (nicknameError != null) {
                                Text(nicknameError!!, color = Color.Red)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { 
                            coroutineScope.launch {
                                isSavingNickname = true
                                try {
                                    vm.updateUserNickname(nicknameInput)
                                } catch (e: NicknameTakenException) {
                                    nicknameError = e.message
                                } finally {
                                    isSavingNickname = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = nicknameInput.isNotBlank() && !isSavingNickname,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF048A81))
                    ) {
                        if (isSavingNickname) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Сохранить никнейм", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LevelLeaderboardCard(level: ApiLevel, isExpanded: Boolean, onExpand: () -> Unit, leaderboard: List<com.example.mazeball.shared.LeaderboardEntry>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF048A81).copy(alpha = 0.9f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExpand)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(level.name, color = Color.White, fontSize = 20.sp)
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                    tint = Color.White
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    if (leaderboard.isEmpty()) {
                        Text("Нет данных о рекордах", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        leaderboard.forEachIndexed { index, entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${index + 1}. ${entry.playerName}", color = Color.White)
                                Text("%.3f".format(entry.timeMillis / 1000.0), color = Color.Yellow)
                            }
                        }
                    }
                }
            }
        }
    }
}
