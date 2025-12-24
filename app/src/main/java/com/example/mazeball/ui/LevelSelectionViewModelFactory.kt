package com.example.mazeball.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mazeball.data.LevelRepository
import com.example.mazeball.data.UserPreferencesRepository

class LevelSelectionViewModelFactory(
    private val levelRepository: LevelRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LevelSelectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LevelSelectionViewModel(levelRepository, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
