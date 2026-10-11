package com.example.practice3kormiletsv.data.repository

import com.example.practice3kormiletsv.data.local.FavoriteDao
import com.example.practice3kormiletsv.data.local.FavoriteEntity
import com.example.practice3kormiletsv.data.local.FavoriteImageStorage
import com.example.practice3kormiletsv.domain.model.Meal
import com.example.practice3kormiletsv.domain.repository.FavoriteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class RoomFavoriteRepository(
    private val dao: FavoriteDao,
    private val images: FavoriteImageStorage
) : FavoriteRepository {
    private val mutex = Mutex()

    override fun observeFavorites() = dao.observeAll().map { favorites -> favorites.map { it.toSummary() } }
    override suspend fun getFavorite(id: String) = dao.find(id)?.toMeal()

    override suspend fun save(meal: Meal) = mutex.withLock {
        withContext(Dispatchers.IO + NonCancellable) {
            val previous = dao.find(meal.summary.id)
            val imagePath = images.download(meal.summary.thumbnailUrl)
            try {
                val summary = meal.summary
                dao.upsert(
                    FavoriteEntity(
                        summary.id, summary.name, summary.category, summary.area, summary.thumbnailUrl,
                        imagePath, meal.ingredients, meal.instructions, meal.sourceUrl, meal.youtubeUrl,
                        System.currentTimeMillis()
                    )
                )
            } catch (error: Exception) {
                images.delete(imagePath)
                throw error
            }
            images.delete(previous?.localImagePath)
        }
    }

    override suspend fun remove(id: String) = mutex.withLock {
        withContext(Dispatchers.IO + NonCancellable) {
            val previous = dao.find(id)
            dao.delete(id)
            images.delete(previous?.localImagePath)
        }
    }
}
