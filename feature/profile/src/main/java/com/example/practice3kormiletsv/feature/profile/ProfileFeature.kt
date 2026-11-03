package com.example.practice3kormiletsv.feature.profile

import android.content.Context
import com.example.practice3kormiletsv.feature.profile.data.local.FileProfileRepository
import com.example.practice3kormiletsv.feature.profile.data.local.LocalAvatarRepository
import com.example.practice3kormiletsv.feature.profile.data.repository.NetworkResumeRepository
import com.example.practice3kormiletsv.feature.profile.domain.usecase.CreateCameraCaptureUseCase
import com.example.practice3kormiletsv.feature.profile.domain.usecase.DeleteDraftPhotoUseCase
import com.example.practice3kormiletsv.feature.profile.domain.usecase.DownloadResumeUseCase
import com.example.practice3kormiletsv.feature.profile.domain.usecase.ImportAvatarUseCase
import com.example.practice3kormiletsv.feature.profile.domain.usecase.ObserveProfileUseCase
import com.example.practice3kormiletsv.feature.profile.domain.usecase.SaveProfileUseCase
import java.io.File
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

class ProfileFeature(context: Context) {
    private val applicationContext = context.applicationContext
    private val profile = FileProfileRepository(File(applicationContext.filesDir, "profile.json"))
    private val avatars = LocalAvatarRepository(applicationContext)
    private val documentClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()
    private val resumes = NetworkResumeRepository(applicationContext, documentClient)

    internal val observeProfile = ObserveProfileUseCase(profile)
    internal val saveProfile = SaveProfileUseCase(profile)
    internal val importAvatar = ImportAvatarUseCase(avatars)
    internal val createCameraCapture = CreateCameraCaptureUseCase(avatars)
    internal val deleteDraftPhoto = DeleteDraftPhotoUseCase(avatars)
    internal val downloadResume = DownloadResumeUseCase(resumes)
}
