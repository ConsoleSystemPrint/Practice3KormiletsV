package com.example.practice3kormiletsv.feature.profile.domain.model

internal data class UserProfile(
    val fullName: String = "",
    val avatarUri: String? = null,
    val resumeUrl: String = ""
)

internal data class ResumeDocument(val uri: String, val name: String, val mimeType: String)

internal enum class ProfileField { NAME, RESUME }

internal class InvalidProfileField(val field: ProfileField) : IllegalArgumentException()
