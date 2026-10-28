package com.example.practice3kormiletsv.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [FavoriteEntity::class], version = 1, exportSchema = true)
@TypeConverters(RecipeConverters::class)
abstract class FavoritesDatabase : RoomDatabase() {
    abstract fun favorites(): FavoriteDao
}
