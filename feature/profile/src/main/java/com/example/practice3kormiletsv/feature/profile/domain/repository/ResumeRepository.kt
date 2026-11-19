package com.example.practice3kormiletsv.feature.profile.domain.repository

import com.example.practice3kormiletsv.feature.profile.domain.model.ResumeDocument

internal interface ResumeRepository {
    suspend fun download(url: String, onProgress: (Int?) -> Unit): ResumeDocument
}
