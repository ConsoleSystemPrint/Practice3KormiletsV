package com.example.practice3kormiletsv.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practice3kormiletsv.domain.model.ResumeDocument
import com.example.practice3kormiletsv.domain.model.UserProfile
import com.example.practice3kormiletsv.domain.usecase.DownloadResumeUseCase
import com.example.practice3kormiletsv.domain.usecase.ObserveProfileUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDownloading: Boolean = false,
    val downloadProgress: Int? = null,
    val downloadError: String? = null
)

class ProfileViewModel(
    private val observeProfile: ObserveProfileUseCase,
    private val downloadResume: DownloadResumeUseCase
) : ViewModel() {
    private val mutableState = MutableStateFlow(ProfileUiState())
    val state = mutableState.asStateFlow()
    private val documentChannel = Channel<ResumeDocument>(Channel.BUFFERED)
    val documents = documentChannel.receiveAsFlow()
    private var profileJob: Job? = null

    init { load() }

    fun load() {
        profileJob?.cancel()
        mutableState.update { it.copy(isLoading = true, error = null) }
        profileJob = viewModelScope.launch {
            try {
                observeProfile().collect { profile ->
                    mutableState.update { it.copy(profile = profile, isLoading = false, error = null) }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(isLoading = false, error = "Не удалось прочитать профиль. Повторите попытку.") }
            }
        }
    }

    fun openResume() {
        val current = state.value
        if (current.isDownloading || current.profile.resumeUrl.isBlank()) return
        mutableState.update { it.copy(isDownloading = true, downloadProgress = null, downloadError = null) }
        viewModelScope.launch {
            try {
                val document = downloadResume(current.profile.resumeUrl) { progress ->
                    mutableState.update { it.copy(downloadProgress = progress) }
                }
                documentChannel.send(document)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(downloadError = "Не удалось скачать резюме. Проверьте ссылку и интернет. Нужна ссылка на файл размером до 30 МБ.") }
            } finally {
                mutableState.update { it.copy(isDownloading = false) }
            }
        }
    }
}
