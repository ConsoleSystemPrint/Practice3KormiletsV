package com.example.practice3kormiletsv.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface MealDbApi {
    @GET("search.php")
    suspend fun search(@Query("s") query: String): MealsResponseDto

    @GET("filter.php")
    suspend fun filter(
        @Query("c") category: String? = null,
        @Query("a") area: String? = null,
        @Query("i") ingredient: String? = null
    ): MealsResponseDto

    @GET("lookup.php")
    suspend fun lookup(@Query("i") id: String): MealsResponseDto

    @GET("list.php")
    suspend fun categories(@Query("c") value: String = "list"): FilterOptionsResponseDto

    @GET("list.php")
    suspend fun areas(@Query("a") value: String = "list"): FilterOptionsResponseDto

    @GET("list.php")
    suspend fun ingredients(@Query("i") value: String = "list"): FilterOptionsResponseDto
}
