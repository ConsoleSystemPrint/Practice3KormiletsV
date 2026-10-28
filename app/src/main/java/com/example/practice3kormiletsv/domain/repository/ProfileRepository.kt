package com.example.practice3kormiletsv.domain.repository

import com.example.practice3kormiletsv.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfile(): Flow<UserProfile>
    suspend fun save(profile: UserProfile)
}
