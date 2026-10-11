package com.example.practice3kormiletsv.domain.cache

import com.example.practice3kormiletsv.domain.model.SearchSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsBadgeCache {
    private val mutableActive = MutableStateFlow(false)
    val isActive = mutableActive.asStateFlow()

    fun update(settings: SearchSettings) {
        mutableActive.value = settings.isActive
    }
}
