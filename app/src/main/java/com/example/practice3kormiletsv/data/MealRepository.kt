package com.example.practice3kormiletsv.data

interface MealRepository {
    fun getMeals(query: String = "", filter: MealFilter? = null): List<MealSummary>
    fun getMeal(id: String): Meal?
    fun getFilterOptions(type: FilterType): List<FilterOption>
}

class MockMealRepository : MealRepository {
    private val meals = MockMeals.all

    override fun getMeals(query: String, filter: MealFilter?): List<MealSummary> {
        val search = query.trim()
        return meals.filter { meal ->
            val matchesName = search.isBlank() ||
                meal.summary.name.contains(search, ignoreCase = true) ||
                meal.summary.originalName.contains(search, ignoreCase = true)
            val matchesFilter = when (filter?.type) {
                FilterType.CATEGORY -> meal.summary.category == filter.value
                FilterType.AREA -> meal.summary.area == filter.value
                FilterType.INGREDIENT -> meal.ingredients.any { it.apiName.equals(filter.value, ignoreCase = true) }
                null -> true
            }
            matchesName && matchesFilter
        }.map { it.summary }
    }

    override fun getMeal(id: String): Meal? = meals.firstOrNull { it.summary.id == id }

    override fun getFilterOptions(type: FilterType): List<FilterOption> = when (type) {
        FilterType.CATEGORY -> meals.map { FilterOption(it.summary.category, it.summary.categoryLabel) }
        FilterType.AREA -> meals.map { FilterOption(it.summary.area, it.summary.areaLabel) }
        FilterType.INGREDIENT -> meals.flatMap { meal -> meal.ingredients.map { FilterOption(it.apiName.lowercase(), it.name) } }
    }.distinctBy { it.value.lowercase() }.sortedBy { it.label }
}
