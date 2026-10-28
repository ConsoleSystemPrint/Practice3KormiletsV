package com.example.practice3kormiletsv.domain.model

data class UserProfile(
    val fullName: String = "",
    val avatarUri: String? = null,
    val resumeUrl: String = ""
)

data class ResumeDocument(val uri: String, val name: String, val mimeType: String)

enum class ProfileField { NAME, RESUME }

class InvalidProfileField(val field: ProfileField) : IllegalArgumentException()
