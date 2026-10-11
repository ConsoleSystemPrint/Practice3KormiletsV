package com.example.practice3kormiletsv.data.repository

import com.example.practice3kormiletsv.domain.model.FilterType
import com.example.practice3kormiletsv.domain.model.MealFilter
import com.example.practice3kormiletsv.domain.repository.FavoriteRepository
import com.example.practice3kormiletsv.domain.repository.MealRepository

class OfflineMealRepository(
    private val network: MealRepository,
    private val favorites: FavoriteRepository
) : MealRepository {
    override fun observeMeals(query: String, filter: MealFilter?, forceRefresh: Boolean) =
        network.observeMeals(query, filter, forceRefresh)

    override suspend fun getMeal(id: String, forceRefresh: Boolean) =
        if (forceRefresh) network.getMeal(id, true) else favorites.getFavorite(id) ?: network.getMeal(id)

    override suspend fun getFilterOptions(type: FilterType, forceRefresh: Boolean) =
        network.getFilterOptions(type, forceRefresh)
}
