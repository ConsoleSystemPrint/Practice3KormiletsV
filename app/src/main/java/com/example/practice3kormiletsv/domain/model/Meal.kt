package com.example.practice3kormiletsv.domain.model

data class MealSummary(
    val id: String,
    val name: String,
    val category: String,
    val area: String,
    val thumbnailUrl: String,
    val localImagePath: String? = null
)

data class Ingredient(val name: String, val measure: String)

data class Meal(
    val summary: MealSummary,
    val ingredients: List<Ingredient>,
    val instructions: List<String>,
    val sourceUrl: String?,
    val youtubeUrl: String?
)

enum class FilterType { CATEGORY, AREA, INGREDIENT }

data class MealFilter(val type: FilterType, val value: String)

data class FilterOption(val value: String)
