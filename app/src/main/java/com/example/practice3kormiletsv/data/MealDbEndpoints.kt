package com.example.practice3kormiletsv.data

import java.net.URLEncoder

object MealDbEndpoints {
    const val TEST_KEY = "1"
    const val BASE_URL = "https://www.themealdb.com/api/json/v1/$TEST_KEY/"

    fun search(name: String): String = endpoint("search.php", "s", name)
    fun lookup(id: String): String = endpoint("lookup.php", "i", id)
    fun filter(type: FilterType, value: String): String = endpoint("filter.php", parameter(type), value)
    fun list(type: FilterType): String = endpoint("list.php", parameter(type), "list")

    private fun parameter(type: FilterType) = when (type) {
        FilterType.CATEGORY -> "c"
        FilterType.AREA -> "a"
        FilterType.INGREDIENT -> "i"
    }

    private fun endpoint(path: String, parameter: String, value: String): String =
        "$BASE_URL$path?key=$TEST_KEY&$parameter=${URLEncoder.encode(value, "UTF-8")}"
}
