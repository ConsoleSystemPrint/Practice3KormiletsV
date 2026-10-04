package com.example.practice3kormiletsv.ui

import com.example.practice3kormiletsv.domain.model.FailureReason
import com.example.practice3kormiletsv.domain.model.FilterOption
import com.example.practice3kormiletsv.domain.model.FilterType
import com.example.practice3kormiletsv.domain.model.MealFailure
import com.example.practice3kormiletsv.domain.model.MealFilter
import com.example.practice3kormiletsv.domain.model.MealSummary

private val categories = mapOf(
    "Beef" to "Говядина", "Chicken" to "Курица", "Dessert" to "Десерты",
    "Lamb" to "Баранина", "Miscellaneous" to "Разное", "Pasta" to "Паста",
    "Pork" to "Свинина", "Seafood" to "Морепродукты", "Side" to "Гарниры",
    "Starter" to "Закуски", "Vegan" to "Веганское", "Vegetarian" to "Вегетарианское",
    "Breakfast" to "Завтраки", "Goat" to "Козлятина"
)

private val areas = mapOf(
    "American" to "США", "British" to "Великобритания", "Canadian" to "Канада",
    "Chinese" to "Китай", "Croatian" to "Хорватия", "Dutch" to "Нидерланды",
    "Egyptian" to "Египет", "Filipino" to "Филиппины", "French" to "Франция",
    "Greek" to "Греция", "Indian" to "Индия", "Irish" to "Ирландия",
    "Italian" to "Италия", "Jamaican" to "Ямайка", "Japanese" to "Япония",
    "Kenyan" to "Кения", "Malaysian" to "Малайзия", "Mexican" to "Мексика",
    "Moroccan" to "Марокко", "Polish" to "Польша", "Portuguese" to "Португалия",
    "Russian" to "Россия", "Spanish" to "Испания", "Thai" to "Таиланд",
    "Tunisian" to "Тунис", "Turkish" to "Турция", "Ukrainian" to "Украина",
    "Vietnamese" to "Вьетнам", "Unknown" to "Другие кухни"
)

val FilterType.label: String
    get() = when (this) {
        FilterType.CATEGORY -> "Категория"
        FilterType.AREA -> "Кухня"
        FilterType.INGREDIENT -> "Ингредиент"
    }

fun FilterOption.label(type: FilterType): String = when (type) {
    FilterType.CATEGORY -> categories[value] ?: value
    FilterType.AREA -> areas[value] ?: value
    FilterType.INGREDIENT -> value
}

val MealFilter.label: String get() = FilterOption(value).label(type)
val MealSummary.categoryLabel: String get() = categories[category] ?: category
val MealSummary.areaLabel: String get() = areas[area] ?: area

fun Exception.userMessage(): String = when ((this as? MealFailure)?.reason) {
    FailureReason.NETWORK -> "Не удалось подключиться к TheMealDB. Проверьте интернет и попробуйте снова."
    FailureReason.TIMEOUT -> "Сервер отвечает слишком долго. Попробуйте ещё раз."
    FailureReason.SERVER -> "TheMealDB временно недоступен. Попробуйте позже."
    FailureReason.INVALID_DATA -> "Не удалось прочитать ответ сервера. Повторите запрос."
    FailureReason.NOT_FOUND -> "Этот рецепт больше не доступен в TheMealDB."
    else -> "Не удалось загрузить данные. Попробуйте снова."
}
