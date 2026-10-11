package com.example.practice3kormiletsv.domain.repository

import com.example.practice3kormiletsv.domain.model.Meal
import com.example.practice3kormiletsv.domain.model.MealSummary
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun observeFavorites(): Flow<List<MealSummary>>
    suspend fun getFavorite(id: String): Meal?
    suspend fun save(meal: Meal)
    suspend fun remove(id: String)
}
