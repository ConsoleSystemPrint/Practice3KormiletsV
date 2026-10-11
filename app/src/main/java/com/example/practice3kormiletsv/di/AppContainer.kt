package com.example.practice3kormiletsv.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.practice3kormiletsv.data.local.FavoritesDatabase
import com.example.practice3kormiletsv.data.local.FavoriteImageStorage
import com.example.practice3kormiletsv.data.local.PreferencesSearchSettingsRepository
import com.example.practice3kormiletsv.data.repository.RoomFavoriteRepository
import com.example.practice3kormiletsv.data.repository.OfflineMealRepository
import com.example.practice3kormiletsv.domain.cache.SettingsBadgeCache
import com.example.practice3kormiletsv.domain.usecase.ObserveSearchSettingsUseCase
import com.example.practice3kormiletsv.domain.usecase.SaveSearchSettingsUseCase
import com.example.practice3kormiletsv.domain.usecase.ObserveFavoritesUseCase
import com.example.practice3kormiletsv.domain.usecase.AddFavoriteUseCase
import com.example.practice3kormiletsv.domain.usecase.RemoveFavoriteUseCase
import java.io.File
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

private val Context.searchSettingsStore by preferencesDataStore(name = "search_settings")

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

    private val database = Room.databaseBuilder(context, FavoritesDatabase::class.java, "favorites.db").build()
    private val imageClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()
    private val favorites = RoomFavoriteRepository(
        database.favorites(),
        FavoriteImageStorage(File(context.filesDir, "favorite_images"), imageClient)
    )
    private val settings = PreferencesSearchSettingsRepository(context.searchSettingsStore)
    private val repository = OfflineMealRepository(NetworkMealRepository(api), favorites)
    val settingsBadgeCache = SettingsBadgeCache()
    val observeSearchSettings = ObserveSearchSettingsUseCase(settings)
    val saveSearchSettings = SaveSearchSettingsUseCase(settings)
    val observeFavorites = ObserveFavoritesUseCase(favorites)
    val removeFavorite = RemoveFavoriteUseCase(favorites)
    val getMeals = GetMealsUseCase(repository)
    val getMeal = GetMealUseCase(repository)
    val addFavorite = AddFavoriteUseCase(getMeal, favorites)
    val getFilterOptions = GetFilterOptionsUseCase(repository)
}
