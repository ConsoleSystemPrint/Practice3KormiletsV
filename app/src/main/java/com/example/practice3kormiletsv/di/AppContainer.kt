package com.example.practice3kormiletsv.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.example.practice3kormiletsv.data.remote.MealDbApi
import com.example.practice3kormiletsv.data.remote.TestKeyInterceptor
import com.example.practice3kormiletsv.data.repository.NetworkMealRepository
import com.example.practice3kormiletsv.domain.usecase.GetFilterOptionsUseCase
import com.example.practice3kormiletsv.domain.usecase.GetMealUseCase
import com.example.practice3kormiletsv.domain.usecase.GetMealsUseCase
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {
    private val client = OkHttpClient.Builder()
        .addInterceptor(TestKeyInterceptor())
        .addInterceptor(
            ChuckerInterceptor.Builder(context)
                .collector(ChuckerCollector(context, showNotification = false))
                .alwaysReadResponseBody(true)
                .build()
        )
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    private val api = Retrofit.Builder()
        .baseUrl("https://www.themealdb.com/api/json/v1/1/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MealDbApi::class.java)

    private val repository = NetworkMealRepository(api)
    val getMeals = GetMealsUseCase(repository)
    val getMeal = GetMealUseCase(repository)
    val getFilterOptions = GetFilterOptionsUseCase(repository)
}
