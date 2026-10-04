package com.example.practice3kormiletsv.domain.repository

import com.example.practice3kormiletsv.domain.model.FilterOption
import com.example.practice3kormiletsv.domain.model.FilterType
import com.example.practice3kormiletsv.domain.model.Meal
import com.example.practice3kormiletsv.domain.model.MealFilter
import com.example.practice3kormiletsv.domain.model.MealSummary
import kotlinx.coroutines.flow.Flow

interface MealRepository {
    fun observeMeals(query: String, filter: MealFilter?, forceRefresh: Boolean): Flow<List<MealSummary>>
    suspend fun getMeal(id: String, forceRefresh: Boolean = false): Meal
    suspend fun getFilterOptions(type: FilterType, forceRefresh: Boolean = false): List<FilterOption>
}
