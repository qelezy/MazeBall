package com.example.mazeball.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mazeball.data.LevelRepository
import com.example.mazeball.data.UserPreferencesRepository
import com.example.mazeball.shared.ApiLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LevelSelectionUiState {
    data object Loading : LevelSelectionUiState
    data class Success(val levels: List<ApiLevel>) : LevelSelectionUiState
    data class Error(val message: String) : LevelSelectionUiState
}

class LevelSelectionViewModel(
    private val levelRepository: LevelRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    var uiState: LevelSelectionUiState by mutableStateOf(LevelSelectionUiState.Loading)
        private set

    private val _bestTimes = MutableStateFlow<Map<Int, Long>>(emptyMap())
    val bestTimes: StateFlow<Map<Int, Long>> = _bestTimes.asStateFlow()

    init {
        loadLevelsAndBestTimes()
    }

    fun loadLevelsAndBestTimes() {
        viewModelScope.launch {
            uiState = LevelSelectionUiState.Loading
            try {
                val levels = levelRepository.getLevels()
                uiState = LevelSelectionUiState.Success(levels)
                _bestTimes.value = userPreferencesRepository.getAllBestTimes()
                
            } catch (e: Exception) {
                uiState = LevelSelectionUiState.Error(e.message ?: "Произошла ошибка")
            }
        }
    }
}
