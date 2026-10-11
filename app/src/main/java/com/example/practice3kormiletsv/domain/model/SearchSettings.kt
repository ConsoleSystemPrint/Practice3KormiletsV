package com.example.practice3kormiletsv.domain.model

data class SearchSettings(val query: String = "", val filter: MealFilter? = null) {
    val isActive: Boolean get() = query.isNotBlank() || filter != null
}
