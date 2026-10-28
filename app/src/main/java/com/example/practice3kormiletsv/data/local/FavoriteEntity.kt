package com.example.practice3kormiletsv.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.practice3kormiletsv.domain.model.Ingredient
import com.example.practice3kormiletsv.domain.model.Meal
import com.example.practice3kormiletsv.domain.model.MealSummary

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val area: String,
    val thumbnailUrl: String,
    val localImagePath: String?,
    val ingredients: List<Ingredient>,
    val instructions: List<String>,
    val sourceUrl: String?,
    val youtubeUrl: String?,
    val savedAt: Long
) {
    fun toSummary() = MealSummary(id, name, category, area, thumbnailUrl, localImagePath)
    fun toMeal() = Meal(toSummary(), ingredients, instructions, sourceUrl, youtubeUrl)
}
