package com.example.practice3kormiletsv.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practice3kormiletsv.domain.cache.SettingsBadgeCache
import com.example.practice3kormiletsv.domain.model.MealFilter
import com.example.practice3kormiletsv.domain.model.MealSummary
import com.example.practice3kormiletsv.domain.model.SearchSettings
import com.example.practice3kormiletsv.domain.usecase.AddFavoriteUseCase
import com.example.practice3kormiletsv.domain.usecase.GetMealsUseCase
import com.example.practice3kormiletsv.domain.usecase.ObserveFavoritesUseCase
import com.example.practice3kormiletsv.domain.usecase.ObserveSearchSettingsUseCase
import com.example.practice3kormiletsv.domain.usecase.RemoveFavoriteUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MealsUiState(
    val query: String = "",
    val filter: MealFilter? = null,
    val meals: List<MealSummary> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val settingsActive: Boolean = false,
    val favorites: List<MealSummary> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val pendingFavoriteIds: Set<String> = emptySet(),
    val favoritesLoading: Boolean = true,
    val favoritesError: String? = null
)

class MealsViewModel(
    private val getMeals: GetMealsUseCase,
    private val observeSearchSettings: ObserveSearchSettingsUseCase,
    private val observeFavorites: ObserveFavoritesUseCase,
    private val addFavorite: AddFavoriteUseCase,
    private val removeFavorite: RemoveFavoriteUseCase,
    private val badgeCache: SettingsBadgeCache
) : ViewModel() {
    private val mutableState = MutableStateFlow(MealsUiState())
    val state = mutableState.asStateFlow()
    private val messageChannel = Channel<String>(Channel.BUFFERED)
    val messages = messageChannel.receiveAsFlow()
    private var mealsJob: Job? = null
    private var favoritesJob: Job? = null
    private var settingsLoaded = false

    init {
        viewModelScope.launch {
            badgeCache.isActive.collect { active -> mutableState.update { it.copy(settingsActive = active) } }
        }
        viewModelScope.launch {
            try {
                observeSearchSettings().collect { settings ->
                    badgeCache.update(settings)
                    mutableState.update { it.copy(query = settings.query, filter = settings.filter) }
                    if (!settingsLoaded) {
                        settingsLoaded = true
                        loadMeals()
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(isLoading = false, error = "Не удалось прочитать настройки. Откройте настройки и повторите попытку.") }
            }
        }
        loadFavorites()
    }

    fun applySettings(settings: SearchSettings) {
        settingsLoaded = true
        badgeCache.update(settings)
        mutableState.update { it.copy(query = settings.query, filter = settings.filter) }
        loadMeals(forceRefresh = true)
    }

    fun refresh() = loadMeals(forceRefresh = true)
    fun retryFavorites() = loadFavorites()

    fun toggleFavorite(id: String) {
        if (id in state.value.pendingFavoriteIds) return
        val removing = id in state.value.favoriteIds
        mutableState.update { it.copy(pendingFavoriteIds = it.pendingFavoriteIds + id) }
        viewModelScope.launch {
            try {
                if (removing) removeFavorite(id) else addFavorite(id)
                if (!removing) messageChannel.send("Рецепт и фото сохранены для просмотра без интернета")
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                messageChannel.send(
                    if (removing) "Не удалось удалить рецепт. Попробуйте ещё раз."
                    else "Не удалось сохранить рецепт и фото. Проверьте интернет и свободное место, затем повторите."
                )
            } finally {
                mutableState.update { it.copy(pendingFavoriteIds = it.pendingFavoriteIds - id) }
            }
        }
    }

    private fun loadMeals(forceRefresh: Boolean = false) {
        mealsJob?.cancel()
        val request = state.value
        mutableState.update { it.copy(isLoading = true, error = null, meals = emptyList()) }
        mealsJob = viewModelScope.launch {
            try {
                getMeals(request.query, request.filter, forceRefresh).collect { meals ->
                    mutableState.update { it.copy(meals = meals, isLoading = false, error = null) }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(isLoading = false, error = error.userMessage()) }
            }
        }
    }

    private fun loadFavorites() {
        favoritesJob?.cancel()
        mutableState.update { it.copy(favoritesLoading = true, favoritesError = null) }
        favoritesJob = viewModelScope.launch {
            try {
                observeFavorites().collect { favorites ->
                    mutableState.update {
                        it.copy(favorites = favorites, favoriteIds = favorites.map { meal -> meal.id }.toSet(), favoritesLoading = false)
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(favoritesLoading = false, favoritesError = "Не удалось прочитать избранное из памяти устройства.") }
            }
        }
    }
}
