package com.example.practice3kormiletsv.data.local

import androidx.room.TypeConverter
import com.example.practice3kormiletsv.domain.model.Ingredient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RecipeConverters {
    private val gson = Gson()

    @TypeConverter
    fun ingredientsToJson(value: List<Ingredient>): String = gson.toJson(value)

    @TypeConverter
    fun ingredientsFromJson(value: String): List<Ingredient> =
        gson.fromJson(value, object : TypeToken<List<Ingredient>>() {}.type)

    @TypeConverter
    fun instructionsToJson(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun instructionsFromJson(value: String): List<String> =
        gson.fromJson(value, object : TypeToken<List<String>>() {}.type)
}
