package com.example.practice3kormiletsv.domain.repository

interface AvatarRepository {
    suspend fun importPhoto(uri: String): String
    suspend fun createCameraCapture(): String
    suspend fun deleteOwnedPhoto(uri: String?)
}
