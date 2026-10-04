package com.example.practice3kormiletsv.domain.usecase

import com.example.practice3kormiletsv.domain.model.FilterType
import com.example.practice3kormiletsv.domain.model.MealFilter
import com.example.practice3kormiletsv.domain.repository.MealRepository

class GetMealsUseCase(private val repository: MealRepository) {
    operator fun invoke(query: String, filter: MealFilter?, forceRefresh: Boolean = false) =
        repository.observeMeals(query.trim(), filter, forceRefresh)
}

class GetMealUseCase(private val repository: MealRepository) {
    suspend operator fun invoke(id: String, forceRefresh: Boolean = false) = repository.getMeal(id, forceRefresh)
}

class GetFilterOptionsUseCase(private val repository: MealRepository) {
    suspend operator fun invoke(type: FilterType, forceRefresh: Boolean = false) = repository.getFilterOptions(type, forceRefresh)
}
