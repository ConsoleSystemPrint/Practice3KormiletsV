package com.example.practice3kormiletsv.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.practice3kormiletsv.domain.model.FilterType
import com.example.practice3kormiletsv.domain.model.MealFilter
import com.example.practice3kormiletsv.domain.model.SearchSettings
import com.example.practice3kormiletsv.domain.repository.SearchSettingsRepository
import kotlinx.coroutines.flow.map

class PreferencesSearchSettingsRepository(private val store: DataStore<Preferences>) : SearchSettingsRepository {
    override val settings = store.data.map { preferences ->
        val type = FilterType.entries.firstOrNull { it.name == preferences[filterTypeKey] }
        val value = preferences[filterValueKey].orEmpty().trim()
        SearchSettings(
            query = preferences[queryKey].orEmpty(),
            filter = if (type != null && value.isNotBlank()) MealFilter(type, value) else null
        )
    }

    override suspend fun save(settings: SearchSettings) {
        store.edit { preferences ->
            preferences[queryKey] = settings.query.trim()
            val filter = settings.filter
            if (filter == null) {
                preferences.remove(filterTypeKey)
                preferences.remove(filterValueKey)
            } else {
                preferences[filterTypeKey] = filter.type.name
                preferences[filterValueKey] = filter.value
            }
        }
    }

    private companion object {
        val queryKey = stringPreferencesKey("query")
        val filterTypeKey = stringPreferencesKey("filter_type")
        val filterValueKey = stringPreferencesKey("filter_value")
    }
}
