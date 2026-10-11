package com.example.practice3kormiletsv.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practice3kormiletsv.domain.cache.SettingsBadgeCache
import com.example.practice3kormiletsv.domain.model.FilterOption
import com.example.practice3kormiletsv.domain.model.FilterType
import com.example.practice3kormiletsv.domain.model.MealFilter
import com.example.practice3kormiletsv.domain.model.SearchSettings
import com.example.practice3kormiletsv.domain.usecase.GetFilterOptionsUseCase
import com.example.practice3kormiletsv.domain.usecase.ObserveSearchSettingsUseCase
import com.example.practice3kormiletsv.domain.usecase.SaveSearchSettingsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchSettingsUiState(
    val query: String = "",
    val filterType: FilterType? = null,
    val filterValue: String = "",
    val options: List<FilterOption> = emptyList(),
    val isLoading: Boolean = true,
    val optionsLoading: Boolean = false,
    val optionsError: String? = null,
    val error: String? = null,
    val isSaving: Boolean = false,
    val savedSettings: SearchSettings? = null
) {
    val canSave: Boolean get() = !isLoading && !isSaving && (filterType == null || filterValue.isNotBlank())
}

class SearchSettingsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val observeSettings: ObserveSearchSettingsUseCase,
    private val saveSettings: SaveSearchSettingsUseCase,
    private val getOptions: GetFilterOptionsUseCase,
    private val badgeCache: SettingsBadgeCache
) : ViewModel() {
    private val mutableState = MutableStateFlow(SearchSettingsUiState())
    val state = mutableState.asStateFlow()
    private var optionsJob: Job? = null

    init { loadSettings() }

    fun loadSettings() {
        mutableState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val settings = observeSettings().first()
                badgeCache.update(settings)
                val restored = savedStateHandle.get<Boolean>("draft_initialized") == true
                val type = if (restored) FilterType.entries.firstOrNull { it.name == savedStateHandle.get<String>("draft_type") } else settings.filter?.type
                mutableState.update {
                    it.copy(
                        query = if (restored) savedStateHandle["draft_query"] ?: "" else settings.query,
                        filterType = type,
                        filterValue = if (restored) savedStateHandle["draft_value"] ?: "" else settings.filter?.value.orEmpty(),
                        isLoading = false
                    )
                }
                rememberDraft()
                loadOptions()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = "Не удалось прочитать настройки. Повторите попытку.") }
            }
        }
    }

    fun setQuery(query: String) {
        if (state.value.isSaving) return
        mutableState.update { it.copy(query = query, error = null) }
        rememberDraft()
    }

    fun setType(type: FilterType?) {
        if (state.value.isSaving || state.value.filterType == type) return
        mutableState.update { it.copy(filterType = type, filterValue = "", options = emptyList(), error = null) }
        rememberDraft()
        loadOptions()
    }

    fun setValue(value: String) {
        if (state.value.isSaving) return
        mutableState.update { it.copy(filterValue = value, error = null) }
        rememberDraft()
    }

    fun reset() {
        if (state.value.isSaving) return
        optionsJob?.cancel()
        mutableState.update { it.copy(query = "", filterType = null, filterValue = "", options = emptyList(), optionsLoading = false, optionsError = null, error = null) }
        rememberDraft()
    }

    fun retryOptions() = loadOptions(forceRefresh = true)

    fun save() {
        if (!state.value.canSave) return
        val draft = state.value
        val settings = SearchSettings(draft.query.trim(), draft.filterType?.let { MealFilter(it, draft.filterValue) })
        mutableState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                saveSettings(settings)
                badgeCache.update(settings)
                mutableState.update { it.copy(isSaving = false, savedSettings = settings) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(isSaving = false, error = "Не удалось сохранить настройки. Проверьте свободное место и повторите.") }
            }
        }
    }

    private fun rememberDraft() {
        val draft = state.value
        savedStateHandle["draft_initialized"] = true
        savedStateHandle["draft_query"] = draft.query
        savedStateHandle["draft_type"] = draft.filterType?.name
        savedStateHandle["draft_value"] = draft.filterValue
    }

    private fun loadOptions(forceRefresh: Boolean = false) {
        optionsJob?.cancel()
        val type = state.value.filterType
        mutableState.update { it.copy(optionsLoading = type != null, optionsError = null) }
        if (type == null) return
        optionsJob = viewModelScope.launch {
            try {
                val options = getOptions(type, forceRefresh)
                mutableState.update { it.copy(options = options, optionsLoading = false) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(optionsLoading = false, optionsError = error.userMessage()) }
            }
        }
    }
}
