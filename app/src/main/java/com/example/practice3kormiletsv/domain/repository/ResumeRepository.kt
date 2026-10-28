package com.example.practice3kormiletsv.domain.repository

import com.example.practice3kormiletsv.domain.model.ResumeDocument

interface ResumeRepository {
    suspend fun download(url: String, onProgress: (Int?) -> Unit): ResumeDocument
}
