package com.example.practice3kormiletsv.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practice3kormiletsv.domain.model.InvalidProfileField
import com.example.practice3kormiletsv.domain.model.ProfileField
import com.example.practice3kormiletsv.domain.model.UserProfile
import com.example.practice3kormiletsv.domain.usecase.CreateCameraCaptureUseCase
import com.example.practice3kormiletsv.domain.usecase.DeleteDraftPhotoUseCase
import com.example.practice3kormiletsv.domain.usecase.ImportAvatarUseCase
import com.example.practice3kormiletsv.domain.usecase.ObserveProfileUseCase
import com.example.practice3kormiletsv.domain.usecase.SaveProfileUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val fullName: String = "",
    val avatarUri: String? = null,
    val resumeUrl: String = "",
    val isLoading: Boolean = true,
    val isProcessingPhoto: Boolean = false,
    val isSaving: Boolean = false,
    val isClosing: Boolean = false,
    val nameError: String? = null,
    val resumeError: String? = null,
    val error: String? = null
) {
    val isBusy: Boolean get() = isProcessingPhoto || isSaving || isClosing
}

sealed interface EditProfileEvent {
    data class OpenCamera(val uri: String) : EditProfileEvent
    data object Exit : EditProfileEvent
}

class EditProfileViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val observeProfile: ObserveProfileUseCase,
    private val saveProfile: SaveProfileUseCase,
    private val importAvatar: ImportAvatarUseCase,
    private val createCameraCapture: CreateCameraCaptureUseCase,
    private val deleteDraftPhoto: DeleteDraftPhotoUseCase
) : ViewModel() {
    private val mutableState = MutableStateFlow(EditProfileUiState())
    val state = mutableState.asStateFlow()
    private val eventChannel = Channel<EditProfileEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()
    private var originalProfile = UserProfile()

    init { load() }

    fun load() {
        mutableState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                originalProfile = observeProfile().first()
                val restored = savedStateHandle.get<Boolean>("profile_draft_initialized") == true
                mutableState.update {
                    it.copy(
                        fullName = if (restored) savedStateHandle["profile_name"] ?: "" else originalProfile.fullName,
                        resumeUrl = if (restored) savedStateHandle["profile_resume"] ?: "" else originalProfile.resumeUrl,
                        avatarUri = if (restored) savedStateHandle["profile_avatar"] else originalProfile.avatarUri,
                        isLoading = false
                    )
                }
                rememberDraft()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = "Не удалось открыть профиль. Повторите попытку.") }
            }
        }
    }

    fun setName(value: String) {
        if (state.value.isBusy) return
        mutableState.update { it.copy(fullName = value, nameError = null, error = null) }
        rememberDraft()
    }

    fun setResumeUrl(value: String) {
        if (state.value.isBusy) return
        mutableState.update { it.copy(resumeUrl = value, resumeError = null, error = null) }
        rememberDraft()
    }

    fun selectGalleryPhoto(uri: String?) {
        if (uri == null || state.value.isBusy) return
        importPhoto(uri)
    }

    fun openCamera() {
        if (state.value.isBusy) return
        mutableState.update { it.copy(isProcessingPhoto = true, error = null) }
        viewModelScope.launch {
            try {
                val uri = createCameraCapture()
                savedStateHandle["camera_capture_uri"] = uri
                eventChannel.send(EditProfileEvent.OpenCamera(uri))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(isProcessingPhoto = false, error = "Не удалось подготовить камеру. Проверьте свободное место.") }
            }
        }
    }

    fun cameraResult(success: Boolean) {
        val uri = savedStateHandle.get<String>("camera_capture_uri")
        savedStateHandle["camera_capture_uri"] = null
        mutableState.update { it.copy(isProcessingPhoto = false) }
        if (uri == null) return
        if (success) importPhoto(uri, deleteSource = true)
        else viewModelScope.launch { deleteQuietly(uri) }
    }

    fun showCameraError(message: String) {
        mutableState.update { it.copy(error = message) }
    }

    fun save() {
        val draft = state.value
        if (draft.isLoading || draft.isBusy) return
        mutableState.update { it.copy(isSaving = true, error = null, nameError = null, resumeError = null) }
        viewModelScope.launch {
            try {
                saveProfile(UserProfile(draft.fullName, draft.avatarUri, draft.resumeUrl))
                if (originalProfile.avatarUri != draft.avatarUri) deleteQuietly(originalProfile.avatarUri)
                eventChannel.send(EditProfileEvent.Exit)
            } catch (error: CancellationException) {
                throw error
            } catch (error: InvalidProfileField) {
                mutableState.update {
                    when (error.field) {
                        ProfileField.NAME -> it.copy(nameError = "Введите ФИО длиной до 120 символов.")
                        ProfileField.RESUME -> it.copy(resumeError = "Введите полную ссылку http:// или https:// на файл.")
                    }
                }
            } catch (error: Exception) {
                mutableState.update { it.copy(error = "Не удалось сохранить профиль. Проверьте свободное место и повторите.") }
            } finally {
                mutableState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun cancel() {
        if (state.value.isBusy) return
        mutableState.update { it.copy(isClosing = true) }
        viewModelScope.launch {
            if (state.value.avatarUri != originalProfile.avatarUri) deleteQuietly(state.value.avatarUri)
            deleteQuietly(savedStateHandle["camera_capture_uri"])
            eventChannel.send(EditProfileEvent.Exit)
        }
    }

    private fun importPhoto(uri: String, deleteSource: Boolean = false) {
        mutableState.update { it.copy(isProcessingPhoto = true, error = null) }
        viewModelScope.launch {
            try {
                val imported = importAvatar(uri)
                val previous = state.value.avatarUri
                mutableState.update { it.copy(avatarUri = imported) }
                rememberDraft()
                if (previous != originalProfile.avatarUri) deleteQuietly(previous)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = "Не удалось открыть фото. Выберите другое изображение размером до 25 МБ.") }
            } finally {
                if (deleteSource) deleteQuietly(uri)
                mutableState.update { it.copy(isProcessingPhoto = false) }
            }
        }
    }

    private suspend fun deleteQuietly(uri: String?) {
        try { deleteDraftPhoto(uri) }
        catch (error: CancellationException) { throw error }
        catch (_: Exception) { }
    }

    private fun rememberDraft() {
        savedStateHandle["profile_draft_initialized"] = true
        savedStateHandle["profile_name"] = state.value.fullName
        savedStateHandle["profile_resume"] = state.value.resumeUrl
        savedStateHandle["profile_avatar"] = state.value.avatarUri
    }
}
