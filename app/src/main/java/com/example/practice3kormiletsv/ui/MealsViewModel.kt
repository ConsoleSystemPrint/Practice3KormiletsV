package com.example.practice3kormiletsv.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.practice3kormiletsv.data.FilterOption
import com.example.practice3kormiletsv.data.FilterType
import com.example.practice3kormiletsv.data.Meal
import com.example.practice3kormiletsv.data.MealFilter
import com.example.practice3kormiletsv.data.MealRepository
import com.example.practice3kormiletsv.data.MealSummary
import com.example.practice3kormiletsv.data.MockMealRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MealsUiState(
    val query: String = "",
    val filter: MealFilter? = null,
    val meals: List<MealSummary> = emptyList(),
    val favorites: List<MealSummary> = emptyList(),
    val favoriteIds: Set<String> = emptySet()
)

class MealsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: MealRepository = MockMealRepository()
) : ViewModel() {
    private val restoredFilter: MealFilter? = savedStateHandle.get<String>("filterType")?.let { type ->
        val filterType = FilterType.entries.firstOrNull { it.name == type } ?: return@let null
        val value = savedStateHandle.get<String>("filterValue") ?: return@let null
        val label = savedStateHandle.get<String>("filterLabel") ?: return@let null
        MealFilter(filterType, value, label)
    }
    private val mutableState = MutableStateFlow(
        MealsUiState(
            query = savedStateHandle["query"] ?: "",
            filter = restoredFilter,
            favoriteIds = (savedStateHandle.get<ArrayList<String>>("favorites") ?: arrayListOf()).toSet()
        )
    )
    val state = mutableState.asStateFlow()

    init { refresh() }

    fun setQuery(query: String) {
        savedStateHandle["query"] = query
        mutableState.value = mutableState.value.copy(query = query)
        refresh()
    }

    fun setFilter(filter: MealFilter?) {
        savedStateHandle["filterType"] = filter?.type?.name
        savedStateHandle["filterValue"] = filter?.value
        savedStateHandle["filterLabel"] = filter?.label
        mutableState.value = mutableState.value.copy(filter = filter)
        refresh()
    }

    fun resetSearch() {
        setQuery("")
        setFilter(null)
    }

    fun toggleFavorite(id: String) {
        if (repository.getMeal(id) == null) return
        val ids = mutableState.value.favoriteIds
        val updated = if (id in ids) ids - id else ids + id
        savedStateHandle["favorites"] = ArrayList(updated)
        mutableState.value = mutableState.value.copy(favoriteIds = updated)
        refresh()
    }

    fun getMeal(id: String): Meal? = repository.getMeal(id)
    fun filterOptions(type: FilterType): List<FilterOption> = repository.getFilterOptions(type)

    private fun refresh() {
        val state = mutableState.value
        mutableState.value = state.copy(
            meals = repository.getMeals(state.query, state.filter),
            favorites = repository.getMeals().filter { it.id in state.favoriteIds }
        )
    }
}
