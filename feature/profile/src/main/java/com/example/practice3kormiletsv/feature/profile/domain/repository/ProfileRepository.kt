package com.example.practice3kormiletsv.feature.profile.domain.repository

import com.example.practice3kormiletsv.feature.profile.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

internal interface ProfileRepository {
    fun observeProfile(): Flow<UserProfile>
    suspend fun save(profile: UserProfile)
}
