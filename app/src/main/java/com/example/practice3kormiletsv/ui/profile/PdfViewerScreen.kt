package com.example.practice3kormiletsv.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.practice3kormiletsv.R
import com.example.practice3kormiletsv.ui.ErrorState
import com.example.practice3kormiletsv.ui.LoadingState

@Composable
fun PdfViewerScreen(state: PdfUiState, onBack: () -> Unit, onRetry: () -> Unit, onPrevious: () -> Unit, onNext: () -> Unit) {
    Scaffold(
        topBar = {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(painterResource(R.drawable.ic_back), "Назад") }
                Text(state.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        bottomBar = {
            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onPrevious, enabled = !state.isLoading && state.page > 0) { Text("Назад") }
                Text(if (state.pageCount > 0) "${state.page + 1} / ${state.pageCount}" else "PDF", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onNext, enabled = !state.isLoading && state.page + 1 < state.pageCount) { Text("Далее") }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).testTag("pdf_viewer")) {
            when {
                state.isLoading -> LoadingState("Открываем PDF…")
                state.error != null -> ErrorState(state.error, onRetry)
                state.bitmap != null -> key(state.page) {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        Image(state.bitmap.asImageBitmap(), "Страница ${state.page + 1}", Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}
