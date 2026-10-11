package com.example.practice3kormiletsv.domain.usecase

import com.example.practice3kormiletsv.domain.model.SearchSettings
import com.example.practice3kormiletsv.domain.repository.FavoriteRepository
import com.example.practice3kormiletsv.domain.repository.SearchSettingsRepository

class ObserveSearchSettingsUseCase(private val repository: SearchSettingsRepository) {
    operator fun invoke() = repository.settings
}

class SaveSearchSettingsUseCase(private val repository: SearchSettingsRepository) {
    suspend operator fun invoke(settings: SearchSettings) = repository.save(settings)
}

class ObserveFavoritesUseCase(private val repository: FavoriteRepository) {
    operator fun invoke() = repository.observeFavorites()
}

class AddFavoriteUseCase(private val getMeal: GetMealUseCase, private val repository: FavoriteRepository) {
    suspend operator fun invoke(id: String) = repository.save(getMeal(id))
}

class RemoveFavoriteUseCase(private val repository: FavoriteRepository) {
    suspend operator fun invoke(id: String) = repository.remove(id)
}
