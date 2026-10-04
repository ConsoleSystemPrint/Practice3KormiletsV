package com.example.practice3kormiletsv.data.mapper

import com.example.practice3kormiletsv.data.remote.MealDto
import com.example.practice3kormiletsv.domain.model.FailureReason
import com.example.practice3kormiletsv.domain.model.Ingredient
import com.example.practice3kormiletsv.domain.model.Meal
import com.example.practice3kormiletsv.domain.model.MealFailure
import com.example.practice3kormiletsv.domain.model.MealSummary
import java.net.URI

fun MealDto.toSummary(category: String = "", area: String = ""): MealSummary {
    val id = idMeal?.trim()?.takeIf { it.isNotEmpty() } ?: throw MealFailure(FailureReason.INVALID_DATA)
    val name = strMeal?.trim()?.takeIf { it.isNotEmpty() } ?: throw MealFailure(FailureReason.INVALID_DATA)
    return MealSummary(
        id = id,
        name = name,
        category = strCategory?.trim()?.takeIf { it.isNotEmpty() } ?: category,
        area = strArea?.trim()?.takeIf { it.isNotEmpty() }
            ?: strCountry?.trim()?.takeIf { it.isNotEmpty() } ?: area,
        thumbnailUrl = strMealThumb.webUrl().orEmpty()
    )
}

fun MealDto.toMeal(): Meal {
    val ingredients = listOf(
        strIngredient1 to strMeasure1,
        strIngredient2 to strMeasure2,
        strIngredient3 to strMeasure3,
        strIngredient4 to strMeasure4,
        strIngredient5 to strMeasure5,
        strIngredient6 to strMeasure6,
        strIngredient7 to strMeasure7,
        strIngredient8 to strMeasure8,
        strIngredient9 to strMeasure9,
        strIngredient10 to strMeasure10,
        strIngredient11 to strMeasure11,
        strIngredient12 to strMeasure12,
        strIngredient13 to strMeasure13,
        strIngredient14 to strMeasure14,
        strIngredient15 to strMeasure15,
        strIngredient16 to strMeasure16,
        strIngredient17 to strMeasure17,
        strIngredient18 to strMeasure18,
        strIngredient19 to strMeasure19,
        strIngredient20 to strMeasure20
    ).mapNotNull { (name, measure) ->
        name?.trim()?.takeIf { it.isNotEmpty() }?.let { Ingredient(it, measure.orEmpty().trim()) }
    }
    val instructions = strInstructions.orEmpty().trim()
        .split(Regex("\\r?\\n+"))
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.matches(Regex("(?i)step\\s*\\d+[:.]?")) }
    return Meal(toSummary(), ingredients, instructions, strSource.webUrl(), strYoutube.webUrl())
}

private fun String?.webUrl(): String? = this?.trim()?.takeIf { value ->
    runCatching { URI(value).let { it.scheme in listOf("https", "http") && !it.host.isNullOrBlank() } }.getOrDefault(false)
}

