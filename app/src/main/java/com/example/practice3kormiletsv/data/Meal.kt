package com.example.practice3kormiletsv.data

import androidx.annotation.DrawableRes

data class MealSummary(
    val id: String,
    val name: String,
    val originalName: String,
    val category: String,
    val categoryLabel: String,
    val area: String,
    val areaLabel: String,
    @param:DrawableRes val imageRes: Int,
    val thumbnailUrl: String
)

data class Ingredient(val name: String, val measure: String, val apiName: String = name)

data class Meal(
    val summary: MealSummary,
    val ingredients: List<Ingredient>,
    val instructions: List<String>,
    val sourceUrl: String?,
    val youtubeUrl: String?
)

enum class FilterType(val label: String) {
    CATEGORY("Категория"), AREA("Кухня"), INGREDIENT("Ингредиент")
}

data class MealFilter(val type: FilterType, val value: String, val label: String)

data class FilterOption(val value: String, val label: String)
