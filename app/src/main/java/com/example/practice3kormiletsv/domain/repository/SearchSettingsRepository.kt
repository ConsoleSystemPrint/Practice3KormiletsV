package com.example.practice3kormiletsv.domain.repository

import com.example.practice3kormiletsv.domain.model.SearchSettings
import kotlinx.coroutines.flow.Flow

interface SearchSettingsRepository {
    val settings: Flow<SearchSettings>
    suspend fun save(settings: SearchSettings)
}
