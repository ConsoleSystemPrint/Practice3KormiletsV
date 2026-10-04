package com.example.practice3kormiletsv.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practice3kormiletsv.domain.model.FilterOption
import com.example.practice3kormiletsv.domain.model.FilterType
import com.example.practice3kormiletsv.domain.model.Meal
import com.example.practice3kormiletsv.domain.model.MealFilter
import com.example.practice3kormiletsv.domain.model.MealSummary
import com.example.practice3kormiletsv.domain.usecase.GetFilterOptionsUseCase
import com.example.practice3kormiletsv.domain.usecase.GetMealUseCase
import com.example.practice3kormiletsv.domain.usecase.GetMealsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MealsUiState(
    val query: String = "",
    val filter: MealFilter? = null,
    val meals: List<MealSummary> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val favorites: List<MealSummary> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val favoritesLoading: Boolean = false,
    val favoritesError: String? = null,
    val filterType: FilterType = FilterType.CATEGORY,
    val filterOptions: Map<FilterType, List<FilterOption>> = emptyMap(),
    val filtersLoading: Boolean = true,
    val filtersError: String? = null
)

class MealsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getMeals: GetMealsUseCase,
    private val getMeal: GetMealUseCase,
    private val getFilterOptions: GetFilterOptionsUseCase
) : ViewModel() {
    private val restoredFilter = savedStateHandle.get<String>("filterType")?.let { type ->
        val filterType = FilterType.entries.firstOrNull { it.name == type } ?: return@let null
        savedStateHandle.get<String>("filterValue")?.let { MealFilter(filterType, it) }
    }
    private val mutableState = MutableStateFlow(
        MealsUiState(
            query = savedStateHandle["query"] ?: "",
            filter = restoredFilter,
            favoriteIds = savedStateHandle.get<ArrayList<String>>("favorites").orEmpty().toSet()
        )
    )
    val state = mutableState.asStateFlow()
    private val knownMeals = mutableMapOf<String, MealSummary>()
    private var mealsJob: Job? = null
    private var filtersJob: Job? = null
    private var favoritesJob: Job? = null

    init {
        loadMeals()
        loadFilterOptions(FilterType.CATEGORY)
        loadFavorites()
    }

    fun setQuery(query: String) {
        if (query == state.value.query) return
        savedStateHandle["query"] = query
        mutableState.update { it.copy(query = query) }
        loadMeals(debounce = true)
    }

    fun setFilter(filter: MealFilter?) {
        if (filter == state.value.filter) return
        savedStateHandle["filterType"] = filter?.type?.name
        savedStateHandle["filterValue"] = filter?.value
        mutableState.update { it.copy(filter = filter) }
        loadMeals()
    }

    fun resetSearch() {
        savedStateHandle["query"] = ""
        savedStateHandle["filterType"] = null
        savedStateHandle["filterValue"] = null
        mutableState.update { it.copy(query = "", filter = null) }
        loadMeals()
    }

    fun refresh() {
        loadMeals(forceRefresh = true)
        if (state.value.filterOptions[FilterType.CATEGORY].isNullOrEmpty()) {
            loadFilterOptions(FilterType.CATEGORY, forceRefresh = true)
        }
    }

    fun selectFilterType(type: FilterType) {
        mutableState.update { it.copy(filterType = type) }
        loadFilterOptions(type)
    }

    fun retryFilters() = loadFilterOptions(state.value.filterType, forceRefresh = true)
    fun retryFavorites() = loadFavorites()

    fun registerMeal(meal: Meal) {
        knownMeals[meal.summary.id] = meal.summary
        updateFavorites()
    }

    fun toggleFavorite(id: String) {
        if (knownMeals[id] == null && id !in state.value.favoriteIds) return
        val updated = state.value.favoriteIds.let { if (id in it) it - id else it + id }
        savedStateHandle["favorites"] = ArrayList(updated)
        mutableState.update { it.copy(favoriteIds = updated) }
        loadFavorites()
    }

    private fun loadMeals(forceRefresh: Boolean = false, debounce: Boolean = false) {
        mealsJob?.cancel()
        val request = state.value
        mutableState.update { it.copy(isLoading = true, error = null, meals = emptyList()) }
        mealsJob = viewModelScope.launch {
            try {
                if (debounce) delay(400)
                getMeals(request.query, request.filter, forceRefresh).collect { meals ->
                    meals.forEach { knownMeals[it.id] = it }
                    mutableState.update { it.copy(meals = meals, isLoading = false, error = null) }
                    updateFavorites()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(isLoading = false, error = error.userMessage()) }
            }
        }
    }

    private fun loadFilterOptions(type: FilterType, forceRefresh: Boolean = false) {
        filtersJob?.cancel()
        mutableState.update { it.copy(filtersLoading = true, filtersError = null) }
        filtersJob = viewModelScope.launch {
            try {
                val options = getFilterOptions(type, forceRefresh)
                mutableState.update { it.copy(filterOptions = it.filterOptions + (type to options), filtersLoading = false) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(filtersLoading = false, filtersError = error.userMessage()) }
            }
        }
    }

    private fun loadFavorites() {
        favoritesJob?.cancel()
        updateFavorites()
        val missing = state.value.favoriteIds.filter { it !in knownMeals }
        mutableState.update { it.copy(favoritesLoading = missing.isNotEmpty(), favoritesError = null) }
        if (missing.isEmpty()) return
        favoritesJob = viewModelScope.launch {
            try {
                missing.forEach { id ->
                    knownMeals[id] = getMeal(id).summary
                    updateFavorites()
                }
                mutableState.update { it.copy(favoritesLoading = false) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(favoritesLoading = false, favoritesError = error.userMessage()) }
            }
        }
    }

    private fun updateFavorites() {
        mutableState.update { current -> current.copy(favorites = current.favoriteIds.mapNotNull { knownMeals[it] }) }
    }
}
