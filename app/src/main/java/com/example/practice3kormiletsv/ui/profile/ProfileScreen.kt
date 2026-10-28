package com.example.practice3kormiletsv.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.practice3kormiletsv.R
import com.example.practice3kormiletsv.ui.ErrorState
import com.example.practice3kormiletsv.ui.LoadingState

@Composable
fun ProfileAvatar(uri: String?, modifier: Modifier = Modifier) {
    Box(modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
        Icon(painterResource(R.drawable.ic_profile), null, Modifier.fillMaxSize().padding(28.dp), tint = MaterialTheme.colorScheme.primary)
        if (uri != null) AsyncImage(uri, "Аватар профиля", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    }
}

@Composable
fun ProfileScreen(state: ProfileUiState, onEdit: () -> Unit, onResume: () -> Unit, onRetry: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.testTag("profile_screen")) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Личная страница", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(6.dp))
                    Text("Профиль", style = MaterialTheme.typography.displaySmall)
                }
                IconButton(onClick = onEdit, enabled = !state.isLoading && state.error == null && !state.isDownloading, modifier = Modifier.testTag("edit_profile")) {
                    Icon(painterResource(R.drawable.ic_edit), "Редактировать профиль")
                }
            }
        }
        when {
            state.isLoading -> item { LoadingState("Открываем профиль…") }
            state.error != null -> item { ErrorState(state.error, onRetry) }
            else -> {
                item {
                    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 30.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            ProfileAvatar(state.profile.avatarUri, Modifier.size(156.dp))
                            Text(state.profile.fullName.ifBlank { "Давайте познакомимся" }, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, modifier = Modifier.testTag("profile_name"))
                            Text(if (state.profile.fullName.isBlank()) "Добавьте имя, фотографию и ссылку на резюме." else "Ваша личная кулинарная книга", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                            if (state.profile.fullName.isBlank()) Button(onClick = onEdit) { Text("Заполнить профиль") }
                        }
                    }
                }
                item {
                    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Icon(painterResource(R.drawable.ic_document), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                            Text("Резюме и портфолио", style = MaterialTheme.typography.titleLarge)
                            Text(state.profile.resumeUrl.ifBlank { "Укажите ссылку на документ в редакторе профиля." }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            Button(onClick = onResume, enabled = state.profile.resumeUrl.isNotBlank() && !state.isDownloading, modifier = Modifier.fillMaxWidth().testTag("open_resume")) {
                                if (state.isDownloading) {
                                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Text("  Скачиваем${state.downloadProgress?.let { " $it%" } ?: "…"}")
                                } else Text("Резюме")
                            }
                            if (state.downloadError != null) Text(state.downloadError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                item {
                    Text("Профиль и аватар сохраняются на этом устройстве. Для скачивания резюме нужен интернет.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
