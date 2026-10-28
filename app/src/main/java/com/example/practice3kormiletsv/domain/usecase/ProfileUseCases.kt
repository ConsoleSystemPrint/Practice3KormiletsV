package com.example.practice3kormiletsv.domain.usecase

import com.example.practice3kormiletsv.domain.model.InvalidProfileField
import com.example.practice3kormiletsv.domain.model.ProfileField
import com.example.practice3kormiletsv.domain.model.UserProfile
import com.example.practice3kormiletsv.domain.repository.AvatarRepository
import com.example.practice3kormiletsv.domain.repository.ProfileRepository
import com.example.practice3kormiletsv.domain.repository.ResumeRepository
import java.net.URI

class ObserveProfileUseCase(private val repository: ProfileRepository) {
    operator fun invoke() = repository.observeProfile()
}

class SaveProfileUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(profile: UserProfile) {
        val normalized = profile.copy(fullName = profile.fullName.trim(), resumeUrl = profile.resumeUrl.trim())
        if (normalized.fullName.isBlank() || normalized.fullName.length > 120) throw InvalidProfileField(ProfileField.NAME)
        if (normalized.resumeUrl.isNotBlank() && !isWebUrl(normalized.resumeUrl)) throw InvalidProfileField(ProfileField.RESUME)
        repository.save(normalized)
    }
}

class ImportAvatarUseCase(private val repository: AvatarRepository) {
    suspend operator fun invoke(uri: String) = repository.importPhoto(uri)
}

class CreateCameraCaptureUseCase(private val repository: AvatarRepository) {
    suspend operator fun invoke() = repository.createCameraCapture()
}

class DeleteDraftPhotoUseCase(private val repository: AvatarRepository) {
    suspend operator fun invoke(uri: String?) = repository.deleteOwnedPhoto(uri)
}

class DownloadResumeUseCase(private val repository: ResumeRepository) {
    suspend operator fun invoke(url: String, onProgress: (Int?) -> Unit) =
        if (isWebUrl(url)) repository.download(url, onProgress) else throw InvalidProfileField(ProfileField.RESUME)
}

private fun isWebUrl(value: String): Boolean = runCatching {
    val uri = URI(value)
    uri.scheme?.lowercase() in setOf("https", "http") && !uri.host.isNullOrBlank() && uri.userInfo == null
}.getOrDefault(false)
