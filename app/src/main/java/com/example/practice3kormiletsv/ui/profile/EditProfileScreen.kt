package com.example.practice3kormiletsv.ui.profile

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.practice3kormiletsv.R
import com.example.practice3kormiletsv.ui.ErrorState
import com.example.practice3kormiletsv.ui.LoadingState

@SuppressLint("InlinedApi")
fun photoPermissions(sdk: Int = Build.VERSION.SDK_INT): Array<String> = when {
    sdk >= 34 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
    sdk >= 33 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
}

private fun hasPhotoPermission(context: Context): Boolean = photoPermissions().any {
    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun EditProfileRoute(viewModel: EditProfileViewModel, onExit: () -> Unit, onMessage: (String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var storageGranted by remember { mutableStateOf(hasPhotoPermission(context)) }
    var storageRequested by rememberSaveable { mutableStateOf(false) }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        viewModel.selectGalleryPhoto(uri?.toString())
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture(), viewModel::cameraResult)
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.openCamera()
        else viewModel.showCameraError("Доступ к камере не разрешён. Можно выбрать фото из галереи.")
    }
    val storagePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        storageGranted = hasPhotoPermission(context)
        if (!storageGranted) {
            onMessage("Доступ к фотографиям не разрешён. Редактирование закрыто.")
            viewModel.cancel()
        }
    }
    LaunchedEffect(Unit) {
        if (!storageGranted && !storageRequested) {
            storageRequested = true
            storagePermission.launch(photoPermissions())
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                EditProfileEvent.Exit -> onExit()
                is EditProfileEvent.OpenCamera -> {
                    try { camera.launch(Uri.parse(event.uri)) }
                    catch (_: ActivityNotFoundException) {
                        viewModel.cameraResult(false)
                        viewModel.showCameraError("На устройстве не найдено приложение камеры.")
                    }
                    catch (_: SecurityException) {
                        viewModel.cameraResult(false)
                        viewModel.showCameraError("Не удалось открыть камеру. Проверьте разрешение в настройках устройства.")
                    }
                }
            }
        }
    }
    BackHandler { if (!state.isBusy) viewModel.cancel() }
    EditProfileScreen(
        state = state, storageGranted = storageGranted,
        onBack = viewModel::cancel, onRetry = viewModel::load,
        onNameChange = viewModel::setName, onResumeChange = viewModel::setResumeUrl, onSave = viewModel::save,
        onGallery = {
            try { gallery.launch(arrayOf("image/*")) }
            catch (_: ActivityNotFoundException) { viewModel.showCameraError("На устройстве не найдено приложение для выбора фотографий.") }
        },
        onCamera = {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) viewModel.openCamera()
            else cameraPermission.launch(Manifest.permission.CAMERA)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileScreen(
    state: EditProfileUiState, storageGranted: Boolean, onBack: () -> Unit, onRetry: () -> Unit,
    onNameChange: (String) -> Unit, onResumeChange: (String) -> Unit, onSave: () -> Unit,
    onGallery: () -> Unit, onCamera: () -> Unit
) {
    var showSources by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    Scaffold(
        topBar = {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, enabled = !state.isBusy) { Icon(painterResource(R.drawable.ic_back), "Назад") }
                Text("Редактирование профиля", style = MaterialTheme.typography.titleLarge)
            }
        },
        bottomBar = {
            Button(onClick = { focusManager.clearFocus(); onSave() }, enabled = storageGranted && !state.isLoading && !state.isBusy, modifier = Modifier.fillMaxWidth().padding(20.dp).testTag("save_profile"), contentPadding = PaddingValues(14.dp)) {
                if (state.isSaving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                else Text("Готово")
            }
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).testTag("edit_profile_screen"), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
            when {
                !storageGranted -> item { LoadingState("Запрашиваем доступ к фотографиям…") }
                state.isLoading -> item { if (state.error != null) ErrorState(state.error, onRetry) else LoadingState("Открываем профиль…") }
                else -> {
                    item {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(Modifier.size(148.dp).clickable(enabled = !state.isBusy) { focusManager.clearFocus(); showSources = true }.testTag("choose_avatar")) {
                                ProfileAvatar(state.avatarUri, Modifier.size(140.dp).align(Alignment.Center))
                                Surface(Modifier.align(Alignment.BottomEnd).size(42.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                                    Box(contentAlignment = Alignment.Center) { Icon(painterResource(R.drawable.ic_camera), "Изменить фото", Modifier.size(22.dp)) }
                                }
                            }
                            TextButton(onClick = { focusManager.clearFocus(); showSources = true }, enabled = !state.isBusy) { Text("Изменить фото") }
                            if (state.isProcessingPhoto) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = state.fullName, onValueChange = onNameChange, label = { Text("ФИО") },
                            placeholder = { Text("Фамилия имя отчество") }, enabled = !state.isBusy,
                            isError = state.nameError != null, supportingText = state.nameError?.let { message -> { Text(message) } },
                            maxLines = 2, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = state.resumeUrl, onValueChange = onResumeChange, label = { Text("Ссылка на резюме или портфолио") },
                            placeholder = { Text("https://example.com/resume.pdf") }, enabled = !state.isBusy,
                            isError = state.resumeError != null,
                            supportingText = { Text(state.resumeError ?: "Прямая ссылка на файл, например PDF.") },
                            maxLines = 3, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().testTag("profile_resume_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )
                    }
                    if (state.error != null) item { Text(state.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
                    item { Text("Нажмите «Готово», чтобы сохранить изменения. При выходе назад изменения отменяются.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
    }
    if (showSources) {
        ModalBottomSheet(onDismissRequest = { showSources = false }) {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Новое фото профиля", style = MaterialTheme.typography.headlineMedium)
                TextButton(onClick = { showSources = false; onGallery() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(painterResource(R.drawable.ic_gallery), null, Modifier.size(24.dp))
                    Text("  Галерея", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                }
                TextButton(onClick = { showSources = false; onCamera() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(painterResource(R.drawable.ic_camera), null, Modifier.size(24.dp))
                    Text("  Камера", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                }
                TextButton(onClick = { showSources = false }, modifier = Modifier.fillMaxWidth()) { Text("Отмена") }
            }
        }
    }
}
