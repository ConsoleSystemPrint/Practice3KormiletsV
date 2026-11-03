package com.example.practice3kormiletsv.feature.profile.domain.repository

internal interface AvatarRepository {
    suspend fun importPhoto(uri: String): String
    suspend fun createCameraCapture(): String
    suspend fun deleteOwnedPhoto(uri: String?)
}
