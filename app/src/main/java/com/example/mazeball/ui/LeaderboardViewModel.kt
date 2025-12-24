package com.example.mazeball.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mazeball.data.LevelRepository
import com.example.mazeball.data.UserPreferences
import com.example.mazeball.data.UserPreferencesRepository
import com.example.mazeball.shared.ApiLevel
import com.example.mazeball.shared.LeaderboardEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface LeaderboardUiState {
    data object Loading : LeaderboardUiState
    data class Success(val levels: List<ApiLevel>, val leaderboards: Map<Int, List<LeaderboardEntry>>) : LeaderboardUiState
    data class Error(val message: String) : LeaderboardUiState
}

class LeaderboardViewModel(
    private val levelRepository: LevelRepository,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    var uiState: LeaderboardUiState by mutableStateOf(LeaderboardUiState.Loading)
        private set

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences("", "", "")
        )

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            uiState = LeaderboardUiState.Loading
            try {
                val levels = levelRepository.getLevels()
                val leaderboards = levelRepository.getAllLeaderboards()
                uiState = LeaderboardUiState.Success(levels, leaderboards)
            } catch (e: Exception) {
                uiState = LeaderboardUiState.Error(e.message ?: "Произошла ошибка")
            }
        }
    }

    suspend fun updateUserNickname(nickname: String) {
        try {
            val newLeaderboards = levelRepository.updateUserNicknameAndSync(nickname)
            val levels = levelRepository.getLevels()
            uiState = LeaderboardUiState.Success(levels, newLeaderboards)
        } catch (e: Exception) {
            println("Ошибка при обновлении никнейма: ${e.message}")
            throw e
        }
    }
    
    suspend fun syncScores() {
        try {
            val newLeaderboards = levelRepository.syncAllBestTimes()
            val levels = levelRepository.getLevels()
            uiState = LeaderboardUiState.Success(levels, newLeaderboards)
        } catch (e: Exception) {
            println("Ошибка синхронизации: ${e.message}")
            throw e
        }
    }
}
