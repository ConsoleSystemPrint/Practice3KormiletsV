package com.example.practice3kormiletsv.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practice3kormiletsv.domain.model.Meal
import com.example.practice3kormiletsv.domain.usecase.GetMealUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MealDetailUiState(val meal: Meal? = null, val isLoading: Boolean = true, val error: String? = null)

class MealDetailViewModel(savedStateHandle: SavedStateHandle, private val getMeal: GetMealUseCase) : ViewModel() {
    private val id = savedStateHandle.get<String>("mealId").orEmpty()
    private val mutableState = MutableStateFlow(MealDetailUiState())
    val state = mutableState.asStateFlow()
    private var job: Job? = null

    init { load() }

    fun retry() = load(forceRefresh = true)

    private fun load(forceRefresh: Boolean = false) {
        job?.cancel()
        mutableState.value = MealDetailUiState()
        job = viewModelScope.launch {
            try {
                val meal = getMeal(id, forceRefresh)
                mutableState.value = MealDetailUiState(meal = meal, isLoading = false)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.value = MealDetailUiState(isLoading = false, error = error.userMessage())
            }
        }
    }
}
